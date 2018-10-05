package io.silverstring.core.service.batch;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.core.provider.MqPublisher;
import io.silverstring.core.provider.wallet.SimpleWalletRpcProvider;
import io.silverstring.core.provider.wallet.WalletRpcProviderFactory;
import io.silverstring.core.repository.hibernate.TransactionRepository;
import io.silverstring.core.repository.hibernate.UserRepository;
import io.silverstring.core.repository.hibernate.WalletRepository;
import io.silverstring.core.service.CoinService;
import io.silverstring.core.service.WalletService;
import io.silverstring.core.util.KeyGenUtil;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.StatusEnum;
import io.silverstring.domain.enums.WalletType;
import io.silverstring.domain.hibernate.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DepositTransactionBatchService {
    final CoinService coinService;
    final WalletService walletService;
    final WalletRpcProviderFactory walletRpcProviderFactory;
    final MqPublisher mqPublisher;
    final TransactionRepository transactionRepository;
    final WalletRepository walletRepository;
    final UserRepository userRepository;

    @Autowired
    public DepositTransactionBatchService(CoinService coinService, WalletService walletService, WalletRpcProviderFactory walletRpcProviderFactory, MqPublisher mqPublisher, TransactionRepository transactionRepository, WalletRepository walletRepository, UserRepository userRepository) {
        this.coinService = coinService;
        this.walletService = walletService;
        this.walletRpcProviderFactory = walletRpcProviderFactory;
        this.mqPublisher = mqPublisher;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @SoftTransational
    public void doPublishTransaction(CoinEnum coinEnum) throws Exception {
        Coin coin = coinService.getCoin(coinEnum);
        if (coin == null) {
            log.warn(">>>>> " + coinEnum.name() +  " Coin is null..");
        } else {
            log.warn(">>>>> " + coinEnum.name() +  " >>>>>>>>>>>>> depositTransactionBatchService.doPublishTransaction start...!!!! [" + coin.getActive().name() + "] >>>>>>>>>>>>");

            SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
            SimpleWalletRpcProvider.Page page = new SimpleWalletRpcProvider.Page();
            page.setPageNo(coin.getDepositScanPageOffset());
            page.setPageSize(coin.getDepositScanPageSize());

            List<WalletDTO.TransactionInfo> transactionInfos = simpleWalletRpcProvider.getTransactions(CategoryEnum.receive, page);

            if(transactionInfos != null) {
                if (transactionInfos.size() > 0) {
                    transactionInfos.stream().forEach(transactionInfo -> {
                        Wallet wallet = walletRepository.findOneByCoinAndAddress(new Coin(coinEnum), transactionInfo.getAddress());
                        if (wallet != null) {
                            transactionInfo.setUserId(wallet.getUserId());
                            transactionInfo.setCoinEnum(coinEnum);

                            String senderAddress = simpleWalletRpcProvider.getSendAddressFromTxId(transactionInfo.getTxid(), transactionInfo);
                            transactionInfo.setAddress(senderAddress);

                            mqPublisher.depositTransactionInfoPublish(transactionInfo);
                        } else {
                            log.warn("wallet is not exist. {}", transactionInfo.getAddress());
                        }
                    });
                }
            }

            log.warn(">>>>> " + coinEnum.name() +  " >>>>>>>>>>>>> depositTransactionBatchService.doPublishTransaction end...!!!! [" + coin.getActive().name() + "] >>>>>>>>>>>>");
        }
    }

    @HardTransational
    public void doTransaction(WalletDTO.TransactionInfo transactionInfo) throws Exception {
        log.debug(">>>>>>>>>>>>>>>> [Listener.mq][depositTransactionBatchService.doTransaction] [" + transactionInfo.getTxid() + "] >>>>>>>>>>>>>>>>>>>>> start >>>>>>>>>");

        Optional<Wallet> existWalletOpt = walletRepository.findOneByUserIdAndCoin(transactionInfo.getUserId(), new Coin(transactionInfo.getCoinEnum()));
        if (!existWalletOpt.isPresent()) {
            log.error("[Listener.mq][depositTransactionBatchService.doTransaction] Not Exist Wallet : {} {} {}", transactionInfo.getUserId(), transactionInfo.getCoinEnum(), transactionInfo.getTxid());
            return;
        }

        Transaction existTransaction = transactionRepository.findOneByCoinAndTxIdAndCategory(new Coin(transactionInfo.getCoinEnum()), transactionInfo.getTxid(), transactionInfo.getCategory());
        if (existTransaction != null && !StatusEnum.PENDING.equals(existTransaction.getStatus()) ) {
            log.info("[Listener.mq][depositTransactionBatchService.doTransaction] Exist Transaction : {}", transactionInfo.getUserId(), transactionInfo.getCoinEnum(), existTransaction.getTxId());
            return;
        }

        Coin coin = coinService.getCoin(transactionInfo.getCoinEnum());
        Long confirmation = transactionInfo.getConfirmations().longValue();

        if (coin.getDepositAllowConfirmation() <= confirmation) {
            String transId = null;
            if (existTransaction != null) {
                transId = existTransaction.getId();

                existTransaction.setRegDt(existTransaction.getRegDt() == null ? LocalDateTime.now() : existTransaction.getRegDt());
                existTransaction.setCompleteDtm(LocalDateTime.now());
                existTransaction.setConfirmation(confirmation);
                existTransaction.setStatus(StatusEnum.COMPLETED);
            } else {
                transId = KeyGenUtil.generateTxId();

                Transaction transaction = new Transaction();
                transaction.setId(KeyGenUtil.generateTxId());
                transaction.setUserId(transactionInfo.getUserId());
                transaction.setCategory(transactionInfo.getCategory());
                transaction.setCoin(new Coin(transactionInfo.getCoinEnum()));
                transaction.setTxId(transactionInfo.getTxid());
                transaction.setAddress(transactionInfo.getAddress());
                transaction.setAmount(transactionInfo.getAmount());
                transaction.setRegDt(LocalDateTime.now());
                transaction.setCompleteDtm(LocalDateTime.now());
                transaction.setConfirmation(confirmation);
                transaction.setStatus(StatusEnum.COMPLETED);
                transactionRepository.save(transaction);
            }

            walletService.increaseAvailableBalance(existWalletOpt.get().getUserId(), existWalletOpt.get().getCoin().getName(), transactionInfo.getAmount(), transId, transactionInfo.getCategory().name(), null, null, null, null);

            log.debug(">>>>>>>>>>>>>>>> [Listener.mq][depositTransactionBatchService.doTransaction] [" + transactionInfo.getTxid() + "] >>>>>>>" + transactionInfo.getAmount().toPlainString());

            BigDecimal realTotalBalance = walletService.getRealWalletTotalBalance(transactionInfo.getCoinEnum(), transactionInfo.getUserId());
            log.debug(">>>>>>>>>>>>>>>> [Listener.mq][depositTransactionBatchService.doTransaction] - realTotalBalance >>>>>>> " + realTotalBalance.toPlainString());

            if ((realTotalBalance != null) && !walletService.isToken(transactionInfo.getCoinEnum())) {
                BigDecimal transferFee = walletService.getTransferFee(transactionInfo.getCoinEnum());
                BigDecimal sendingAmount = WalletUtil.scale(realTotalBalance.subtract(transferFee));
                if (sendingAmount.doubleValue() > 0 && realTotalBalance.doubleValue() > transferFee.doubleValue() &&  realTotalBalance.doubleValue() > coin.getAutoCollectMinAmount().doubleValue()) {
                    String adminTransId = KeyGenUtil.generateTxId();

                    AdminWallet adminWallet = walletService.increaseAvailableAdminBalance(transactionInfo.getCoinEnum(), WalletType.COLD, sendingAmount, adminTransId, CategoryEnum.send_cold.name(), transId);
                    log.info("[Listener.mq][depositTransactionBatchService.doTransaction] adminWallet : {} {} {}", adminWallet.getAddress(), realTotalBalance.toPlainString(), sendingAmount.toPlainString());

                    User user = userRepository.findOne(existWalletOpt.get().getUserId());
                    String txId = walletService.sendTo(transactionInfo.getCoinEnum(), user.getId(), user.getEmail(), existWalletOpt.get().getAddress(), adminWallet.getAddress(), sendingAmount);
                    log.info("[Listener.mq][depositTransactionBatchService.doTransaction] Admin_Collect_txId : {} ", txId);

                    Transaction coldTransaction = new Transaction();
                    coldTransaction.setId(adminTransId);
                    coldTransaction.setUserId(transactionInfo.getUserId());
                    coldTransaction.setCategory(CategoryEnum.send_cold);
                    coldTransaction.setCoin(new Coin(transactionInfo.getCoinEnum()));
                    coldTransaction.setTag(transferFee.toPlainString());
                    coldTransaction.setTxId(txId);
                    coldTransaction.setAddress(adminWallet.getAddress());
                    coldTransaction.setAmount(sendingAmount);
                    coldTransaction.setRegDt(LocalDateTime.now());
                    coldTransaction.setCompleteDtm(LocalDateTime.now());
                    coldTransaction.setConfirmation(10000l);
                    coldTransaction.setStatus(StatusEnum.COMPLETED);
                    coldTransaction.setReason("cold wallet auto sending");
                    transactionRepository.save(coldTransaction);
                } else {
                    log.debug("[Listener.mq][depositTransactionBatchService.doTransaction] >>>>>>>>>>>>>> don't send to coldwallet" + transactionInfo.getTxid());
                }
            }
        } else {
            if (existTransaction != null) {
                existTransaction.setRegDt(existTransaction.getRegDt() == null ? LocalDateTime.now() : existTransaction.getRegDt());
                existTransaction.setCompleteDtm(null);
                existTransaction.setConfirmation(confirmation);
                existTransaction.setStatus(StatusEnum.PENDING);
            } else {
                Transaction transaction = new Transaction();
                transaction.setId(KeyGenUtil.generateTxId());
                transaction.setUserId(transactionInfo.getUserId());
                transaction.setCategory(transactionInfo.getCategory());
                transaction.setCoin(new Coin(transactionInfo.getCoinEnum()));
                transaction.setTxId(transactionInfo.getTxid());
                transaction.setAddress(transactionInfo.getAddress());
                transaction.setAmount(transactionInfo.getAmount());
                transaction.setRegDt(LocalDateTime.now());
                transaction.setCompleteDtm(null);
                transaction.setConfirmation(confirmation);
                transaction.setStatus(StatusEnum.PENDING);
                transactionRepository.save(transaction);
            }
        }

        log.debug(">>>>>>>>>>>>>>>> [Listener.mq] depositTransactionBatchService.doTransaction [" + transactionInfo.getTxid() + "] >>>>>>>>>>>>>>>>>>>>> end >>>>>>>>>");
    }
}
