package io.silverstring.core.service.batch;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.core.provider.MqPublisher;
import io.silverstring.core.provider.wallet.WalletRpcProviderFactory;
import io.silverstring.core.repository.hibernate.ManualTransactionRepository;
import io.silverstring.core.repository.hibernate.TransactionRepository;
import io.silverstring.core.repository.hibernate.WalletRepository;
import io.silverstring.core.service.CoinService;
import io.silverstring.core.service.TransactionService;
import io.silverstring.core.service.WalletService;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.StatusEnum;
import io.silverstring.domain.enums.WalletType;
import io.silverstring.domain.hibernate.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class WithdrawalTransactionBatchService {
    final CoinService coinService;
    final TransactionService transactionService;
    final WalletRpcProviderFactory walletRpcProviderFactory;
    final MqPublisher mqPublisher;
    final TransactionRepository transactionRepository;
    final ManualTransactionRepository manualTransactionRepository;
    final WalletRepository walletRepository;
    final WalletService walletService;

    @Autowired
    public WithdrawalTransactionBatchService(CoinService coinService, TransactionService transactionService, WalletRpcProviderFactory walletRpcProviderFactory, MqPublisher mqPublisher, TransactionRepository transactionRepository, ManualTransactionRepository manualTransactionRepository, WalletRepository walletRepository, WalletService walletService) {
        this.coinService = coinService;
        this.transactionService = transactionService;
        this.walletRpcProviderFactory = walletRpcProviderFactory;
        this.mqPublisher = mqPublisher;
        this.transactionRepository = transactionRepository;
        this.manualTransactionRepository = manualTransactionRepository;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
    }

    @SoftTransational
    public void doPublishTransaction(CoinEnum coinEnum) {
        Coin coin = coinService.getCoin(coinEnum);
        if (coin == null) {
            log.warn(">>>>> " + coinEnum.name() +  " Coin is null....");
        } else {
            log.warn(">>>>> " + coinEnum.name() +  " >>>>>>>>>> withdrawalTransactionBatchService.doPublishTransaction  >>>>>>>>>>>>>    start...!!!! ");

            Page<Transaction> transactionsPage = transactionRepository.findAllByCoinAndCategoryAndStatus(new Coin(coinEnum), CategoryEnum.send, StatusEnum.APPROVAL, new PageRequest(0, 100));
            for (Transaction transaction : transactionsPage.getContent()) {
                log.info("WithdrawalTransactionBatchService:doPublishTransaction:{}", transaction);

                WalletDTO.TransactionInfo transactionInfo = new WalletDTO.TransactionInfo();
                transactionInfo.setUserId(transaction.getUserId());
                transactionInfo.setCoinEnum(transaction.getCoin().getName());
                transactionInfo.setAddress(transaction.getAddress());
                transactionInfo.setCategory(transaction.getCategory());
                transactionInfo.setAmount(transaction.getAmount());
                transactionInfo.setConfirmations(new BigInteger(transaction.getConfirmation().toString()));
                transactionInfo.setTxid(transaction.getTxId());
                transactionInfo.setBankNm(transaction.getBankNm());
                transactionInfo.setRecvNm(transaction.getRecvNm());
                mqPublisher.withdrawalTransactionInfoPublish(transactionInfo);
            }

            log.warn(">>>>> " + coinEnum.name() +  ">>>>>>>>>> withdrawalTransactionBatchService.doPublishTransaction  >>>>>>>>>>>>>    end...!!!! ");
        }
    }

    @HardTransational
    public void doTransaction(WalletDTO.TransactionInfo transactionInfo) throws Exception {
        log.debug(">>>>>>>>>>>>>>>> [Listener.mq][withdrawalTransactionBatchService.doTransaction] [" + transactionInfo.getTxid() + "] >>>>>>>>>>>>>>>>>>>>> start >>>>>>>>>");

        Coin coin = coinService.getCoin(transactionInfo.getCoinEnum());
        if (coin == null) {
            log.error("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Coin is nul.... ");
            return;
        }

        Optional<Wallet> existWalletOpt = walletRepository.findOneByUserIdAndCoin(transactionInfo.getUserId(), new Coin(transactionInfo.getCoinEnum()));
        if (!existWalletOpt.isPresent()) {
            log.error("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Not Exist Wallet : {} {} {}", transactionInfo.getUserId(), transactionInfo.getCoinEnum(), transactionInfo.getTxid());
            return;
        }

        Transaction existTransaction = transactionRepository.findOneByCoinAndTxIdAndCategory(new Coin(transactionInfo.getCoinEnum()), transactionInfo.getTxid(), transactionInfo.getCategory());
        if (existTransaction == null) {
            log.error("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Not Exist Transaction : {} {} {}", transactionInfo.getUserId(), transactionInfo.getCoinEnum(), transactionInfo.getTxid());
            return;
        }

        if (existTransaction != null && !StatusEnum.APPROVAL.equals(existTransaction.getStatus()) ) {
            log.error("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Exist Transaction : {}", transactionInfo.getUserId(), transactionInfo.getCoinEnum(), existTransaction.getTxId());
            return;
        }

        ManualTransaction existManualTransaction = manualTransactionRepository.findByIdAndUserId(transactionInfo.getTxid(), transactionInfo.getUserId());
        if (existManualTransaction == null) {
            log.error("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Not Exist ManualTransaction : {} {}", existManualTransaction.getUserId(), existManualTransaction.getId());
            return;
        }

        existTransaction.setRegDt(existTransaction.getRegDt() == null ? LocalDateTime.now() : existTransaction.getRegDt());
        existTransaction.setCompleteDtm(LocalDateTime.now());
        existTransaction.setConfirmation(10000l);
        existTransaction.setStatus(StatusEnum.COMPLETED);

        existManualTransaction.setRegDt(existManualTransaction.getRegDt() == null ? LocalDateTime.now() : existManualTransaction.getRegDt());
        existManualTransaction.setStatus(StatusEnum.COMPLETED);

        walletService.decreaseUsingBalance(existWalletOpt.get().getUserId(), existWalletOpt.get().getCoin().getName(), WalletUtil.scale(transactionInfo.getAmount()), existTransaction.getId(), existTransaction.getCategory().name(), null, null, null, null);

        BigDecimal withdrawalFee = walletService .getWithdrawalFee(transactionInfo.getCoinEnum());
        BigDecimal sendingAmount = WalletUtil.scale(transactionInfo.getAmount().subtract(withdrawalFee));

        if (sendingAmount.doubleValue() > 0) {
            log.debug("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>>> sendingAmount               : " +  transactionInfo.getCoinEnum().name() + ":" + transactionInfo.getAddress() + ":" + sendingAmount.toPlainString() + " start >>>>>>>>>>>>>>>> ");
            log.debug("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>>> transactionInfo.getAmount() : " +  transactionInfo.getAmount());
            log.debug("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>>> withdrawalFee               : " +  withdrawalFee);

            AdminWallet adminWallet = walletService.decreaseAvailableAdminBalance(transactionInfo.getCoinEnum(), WalletType.HOT, sendingAmount, existTransaction.getId(), existTransaction.getCategory().name(), null);
            log.info("[Listener.mq][withdrawalTransactionBatchService.doTransaction] adminWallet : {} {} {}", adminWallet.getAddress(), sendingAmount.toPlainString());

            if(walletService.isToken(transactionInfo.getCoinEnum())) {
                BigDecimal transferFee = walletService.getTransferFee(transactionInfo.getCoinEnum());
                AdminWallet adminWalletFee = walletService.decreaseGasAdminBalance(transactionInfo.getCoinEnum(), WalletType.HOT, transferFee, existTransaction.getId(), existTransaction.getCategory().name(), "transfer fee");
                log.info("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>> transfer fee >>>> adminWallet : {} {} {}", adminWalletFee.getAddress(), sendingAmount.toPlainString());
            } else {
                AdminWallet adminWalletFee = walletService.decreaseAvailableAdminBalance(transactionInfo.getCoinEnum(), WalletType.HOT, withdrawalFee, existTransaction.getId(), existTransaction.getCategory().name(), "transfer fee");
                log.info("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>> transfer fee >>>> adminWallet : {} {} {}", adminWalletFee.getAddress(), sendingAmount.toPlainString());
            }

            String txId = walletService.sendFromHotWallet(transactionInfo.getCoinEnum(), transactionInfo.getAddress(), sendingAmount);

            log.info("[Listener.mq][withdrawalTransactionBatchService.doTransaction] Admin_txId : {} ", txId);
            log.debug("[Listener.mq][withdrawalTransactionBatchService.doTransaction] >>>>> sendingAmount               : " +  transactionInfo.getCoinEnum().name() + " : " + transactionInfo.getAddress() + " : " + sendingAmount.toPlainString() + " end >>>>>>>>>>>>>>>> ");

            existTransaction.setTxId(txId);
        } else {
            log.debug("[Listener.mq][withdrawalTransactionBatchService.doTransaction] sendingAmount is zero >>> " + transactionInfo.getCoinEnum().name() + ":" + sendingAmount.toPlainString() + ":" + withdrawalFee.toPlainString());

            walletService.increaseUsingBalance(existWalletOpt.get().getUserId(), existWalletOpt.get().getCoin().getName(), WalletUtil.scale(transactionInfo.getAmount()), existTransaction.getId(), existTransaction.getCategory().name(), null, null, null, null);
            transactionService.withdrawalCancel(transactionInfo.getUserId(), transactionInfo.getTxid(), "Insufficient withdrawal amount", true);
        }

        log.debug(">>>>>>>>>>>>>>>> [Listener.mq][withdrawalTransactionBatchService.doTransaction] [" + transactionInfo.getTxid() + "] >>>>>>>>>>>>>>>>>>>>> end >>>>>>>>>");
    }
}
