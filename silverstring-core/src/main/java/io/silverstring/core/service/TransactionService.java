package io.silverstring.core.service;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.core.exception.ExchangeException;
import io.silverstring.core.repository.hibernate.*;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.TransactionDTO;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.StatusEnum;
import io.silverstring.domain.enums.WalletType;
import io.silverstring.domain.hibernate.*;
import io.silverstring.domain.util.CompareUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class TransactionService {
    private final UserRepository userRepository;
    private final ManualTransactionRepository manualTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AdminWalletRepository adminWalletRepository;
    private final LevelRepository levelRepository;
    private final CoinRepository coinRepository;
    private final ModelMapper modelMapper;
    private final OtpService otpService;
    private final WalletService walletService;

    @Autowired
    public TransactionService(UserRepository userRepository, ManualTransactionRepository manualTransactionRepository, TransactionRepository transactionRepository, WalletRepository walletRepository, AdminWalletRepository adminWalletRepository, LevelRepository levelRepository, CoinRepository coinRepository, ModelMapper modelMapper, OtpService otpService, WalletService walletService) {
        this.userRepository = userRepository;
        this.manualTransactionRepository = manualTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.adminWalletRepository = adminWalletRepository;
        this.levelRepository = levelRepository;
        this.coinRepository = coinRepository;
        this.modelMapper = modelMapper;
        this.otpService = otpService;
        this.walletService = walletService;
    }

    public TransactionDTO.ResTransactions getTransactionsWithoutCategory(Long userId, TransactionDTO.ReqTransactions reqTransactions) {
        Page<Transaction> transactionPage = transactionRepository.findAllByUserIdOrderByRegDtDesc(userId, new PageRequest(reqTransactions.getPageNo(),reqTransactions.getPageSize()));
        return TransactionDTO.ResTransactions.builder()
                .pageNo(reqTransactions.getPageNo())
                .pageSize(reqTransactions.getPageSize())
                .pageTotalCnt(transactionPage.getTotalPages())
                .transactions(transactionPage.getContent())
                .build();
    }

    public TransactionDTO.ResTransactions getTransactions(Long userId, TransactionDTO.ReqTransactions reqTransactions) {
        Page<Transaction> transactionPage = transactionRepository.findAllByUserIdAndCoinAndCategoryOrderByRegDtDesc(userId, new Coin(reqTransactions.getCoinName()), reqTransactions.getCategory(), new PageRequest(reqTransactions.getPageNo(),reqTransactions.getPageSize()));
        return TransactionDTO.ResTransactions.builder()
                .pageNo(reqTransactions.getPageNo())
                .pageSize(reqTransactions.getPageSize())
                .pageTotalCnt(transactionPage.getTotalPages())
                .transactions(transactionPage.getContent())
                .build();
    }

    @HardTransational
    public ManualTransaction regist(User user, ManualTransaction manualTransaction, TransactionDTO.ReqWithdrawal request) {
        Coin coin = coinRepository.findOne(manualTransaction.getCoin().getName()); // 코인정보

        if(CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(manualTransaction.getAmount()))) {
            throw new ExchangeException(CodeEnum.NUMBER_IS_NEGATIVE);
        }

        User existUser = userRepository.findOne(user.getId());
        if ((existUser == null) || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        Optional<Wallet> existWalletOp = walletRepository.findOneByUserIdAndCoin(manualTransaction.getUserId(), manualTransaction.getCoin());
        if (!existWalletOp.isPresent()) {
            throw new ExchangeException(CodeEnum.WALLET_NOT_EXIST);
        }

        if((request.getOtp() == null) || request.getOtp().equals("")) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        } else if(!otpService.isOtpValid(existUser, request.getOtp())) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        Wallet wallet = existWalletOp.get();
        if (CompareUtil.Condition.LT.equals(CompareUtil.compare(manualTransaction.getAmount(), wallet.getCoin().getWithdrawalMinAmount()))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_OVER_MINAMOUNT);
        }

        BigDecimal withdrawalFee = walletService .getWithdrawalFee(coin.getName());
        if (CompareUtil.Condition.LT.equals(CompareUtil.compare(manualTransaction.getAmount(), withdrawalFee))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_UNDER_FEE);
        } else if(CompareUtil.Condition.EQ.equals(CompareUtil.compare(manualTransaction.getAmount(), withdrawalFee))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_UNDER_FEE);
        }

        BigDecimal realWithdrawalAmount = wallet.getAvailableBalance().subtract(coin.getWithdrawalMinAmount());
        if(CompareUtil.Condition.GT.equals(CompareUtil.compare(manualTransaction.getAmount(), realWithdrawalAmount))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_OVER_REALAVAIL);
        }

        Level level = levelRepository.findOneByCoinNameAndLevel(manualTransaction.getCoin().getName(), existUser.getLevel());
        BigDecimal myLimitAmount = new BigDecimal(0);
        if(CompareUtil.Condition.GT.equals(CompareUtil.compareToZero(level.getOnedayAmount().subtract(wallet.getTodayWithdrawalTotalBalance())))) {
            myLimitAmount = level.getOnedayAmount().subtract(wallet.getTodayWithdrawalTotalBalance());
        }

        if (CompareUtil.Condition.GT.equals(CompareUtil.compare(manualTransaction.getAmount(), myLimitAmount))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_OVER_ONEDAYRESIDUAL);
        }

        if (CompareUtil.Condition.GT.equals(CompareUtil.compare(manualTransaction.getAmount(), level.getOnedayAmount()))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_OVER_ONEDAYLIMIT);
        }

        if (CompareUtil.Condition.GT.equals(CompareUtil.compare(manualTransaction.getAmount(), level.getOnceAmount()))) {
            throw new ExchangeException(CodeEnum.WITHDWRL_AMOUNT_OVER_ONETIMELIMIT);
        }

        Optional<AdminWallet> adminWalletOp = adminWalletRepository.findOneByCoinNameAndType(manualTransaction.getCoin().getName(), WalletType.HOT);
        if (CompareUtil.Condition.GT.equals(CompareUtil.compare(adminWalletOp.get().getAvailableBalance(), request.getAmount()))) {
            if (CompareUtil.Condition.GT.equals(CompareUtil.compare(coin.getWithdrawalAutoAllowMaxAmount(), request.getAmount()))) {
                manualTransaction.setStatus(StatusEnum.APPROVAL);// 자동출금 처리
            }
        }

        TransactionPK transactionPK = new TransactionPK();
        transactionPK.setId(manualTransaction.getId());
        transactionPK.setUserId(manualTransaction.getUserId());
        Transaction existTransaction = transactionRepository.findOne(transactionPK);
        if (existTransaction != null) {
            throw new ExchangeException(CodeEnum.ALREADY_TRANSACTION_EXIST);
        }

        Transaction transaction = modelMapper.map(manualTransaction, Transaction.class);
        transaction.setTxId(manualTransaction.getId());
        transaction.setConfirmation(0l);
        transactionRepository.save(transaction);

        walletService.decreaseAvailableBalance(existUser.getId(), manualTransaction.getCoin().getName(), WalletUtil.scale(manualTransaction.getAmount()), manualTransaction.getId(), manualTransaction.getCategory().name(), null, null, null, null);// 사용가능한금액 = 사용가능한잔액 - 출금요청액
        walletService.increaseUsingBalance(existUser.getId(), manualTransaction.getCoin().getName(), WalletUtil.scale(manualTransaction.getAmount()), manualTransaction.getId(), manualTransaction.getCategory().name(), null, null, null, null);// 현재사용중인금액 = 현재사용중인잔액 + 출금요청액
        walletService.increaseTodayWithdrawalTotalBalance(existUser.getId(), manualTransaction.getCoin().getName(), WalletUtil.scale(manualTransaction.getAmount()));// 오늘출금전체잔액 = 오늘출금전체잔액 + 출금요청액

        ManualTransactionPK manualTransactionPK = new ManualTransactionPK();
        manualTransactionPK.setId(manualTransaction.getId());
        manualTransactionPK.setUserId(existUser.getId());
        ManualTransaction existManualTransaction = manualTransactionRepository.findOne(manualTransactionPK);
        if (existManualTransaction != null) {
            throw new ExchangeException(CodeEnum.ALREADY_MANUAL_TRANSACTION_EXIST);
        }

        return manualTransactionRepository.save(manualTransaction);
    }

    @HardTransational
    public TransactionDTO.ResCancelWithdrawal cancel(User user, TransactionDTO.ReqCancelWithdrawal request) {
        String transaction_id = withdrawalCancel(user.getId(), request.getTransaction_id(), "user cancel", false);
        return TransactionDTO.ResCancelWithdrawal.builder().transaction_id(transaction_id).build();
    }

    @HardTransational
    public String withdrawalCancel(Long userId, String transaction_id, String reason, boolean forceCancel) throws ExchangeException {
        User existUser = userRepository.findOne(userId);
        if ((existUser == null) || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        ManualTransactionPK manualTransactionPK = new ManualTransactionPK();
        manualTransactionPK.setId(transaction_id);
        manualTransactionPK.setUserId(userId);
        ManualTransaction existManualTransaction = manualTransactionRepository.findOne(manualTransactionPK);
        if(existManualTransaction == null) {
            throw new ExchangeException(CodeEnum.MANUAL_TRANSACTION_NOT_EXIST);
        }

        TransactionPK transactionPK = new TransactionPK();
        transactionPK.setId(transaction_id);
        transactionPK.setUserId(userId);
        Transaction existTransaction = transactionRepository.findOne(transactionPK);
        if(existTransaction == null) {
            throw new ExchangeException(CodeEnum.TRANSACTION_NOT_EXIST);
        }

        if(!forceCancel) {
            if (!StatusEnum.PENDING.equals(existTransaction.getStatus())) {
                throw new ExchangeException(CodeEnum.ALREADY_STATUS_IS_NOT_PENDING);
            }
        }

        Optional<Wallet> existWalletOp = walletRepository.findOneByUserIdAndCoin(userId, existTransaction.getCoin());
        if (!existWalletOp.isPresent()) {
            throw new ExchangeException(CodeEnum.WALLET_NOT_EXIST);
        }

        existManualTransaction.setStatus(StatusEnum.CANCEL);
        existManualTransaction.setReason(reason);

        existTransaction.setStatus(StatusEnum.CANCEL);
        existTransaction.setReason(reason);
        existTransaction.setCompleteDtm(LocalDateTime.now());

        Wallet userWallet = existWalletOp.get();
        walletService.increaseAvailableBalance(userWallet.getUserId(), userWallet.getCoin().getName(), existManualTransaction.getAmount(), existTransaction.getId(), existTransaction.getCategory().name(), null, null, null, null);
        walletService.decreaseUsingBalance(userWallet.getUserId(), userWallet.getCoin().getName(), existManualTransaction.getAmount(), existTransaction.getId(), existTransaction.getCategory().name(), null, null, null, null);
        walletService.decreaseTodayWithdrawalTotalBalance(userWallet.getUserId(), userWallet.getCoin().getName(), existManualTransaction.getAmount());

        return transaction_id;
    }

    @SoftTransational
    public ManualTransaction registManualTransaction(ManualTransaction manualTransaction) {
        ManualTransactionPK manualTransactionPK = new ManualTransactionPK();
        manualTransactionPK.setId(manualTransaction.getId());
        manualTransactionPK.setUserId(manualTransaction.getUserId());
        ManualTransaction existManualTransaction = manualTransactionRepository.findOne(manualTransactionPK);
        if (existManualTransaction != null) {
            throw new ExchangeException(CodeEnum.ALREADY_MANUAL_TRANSACTION_EXIST);
        }

        return manualTransactionRepository.save(manualTransaction);
    }

    @SoftTransational
    public void updateStatusManualTransaction(ManualTransactionPK manualTransactionPK, StatusEnum status, String reason) {
        ManualTransaction manualTransaction = manualTransactionRepository.findOne(manualTransactionPK);
        if (manualTransaction == null) {
            throw new ExchangeException(CodeEnum.MANUAL_TRANSACTION_NOT_EXIST);
        }

        if (!StatusEnum.PENDING.equals(manualTransaction.getStatus())) {
            throw new ExchangeException(CodeEnum.ALREADY_STATUS_IS_NOT_PENDING);
        }

        manualTransaction.setStatus(status);
        manualTransaction.setReason(reason);
    }

    @SoftTransational
    public Transaction registTransaction(Transaction transaction) {
        TransactionPK transactionPK = new TransactionPK();
        transactionPK.setId(transaction.getId());
        transactionPK.setUserId(transaction.getUserId());
        Transaction existTransaction = transactionRepository.findOne(transactionPK);
        if (existTransaction != null) {
            throw new ExchangeException(CodeEnum.ALREADY_TRANSACTION_EXIST);
        }

        return transactionRepository.save(transaction);
    }

    @SoftTransational
    public void updateStatusTransaction(TransactionPK transactionPK, StatusEnum status, String reason) {
        Transaction transaction = transactionRepository.findOne(transactionPK);
        if (transaction == null) {
            throw new ExchangeException(CodeEnum.TRANSACTION_NOT_EXIST);
        }

        if (!StatusEnum.PENDING.equals(transaction.getStatus())) {
            throw new ExchangeException(CodeEnum.ALREADY_STATUS_IS_NOT_PENDING);
        }

        transaction.setStatus(status);
        transaction.setReason(reason);
        transaction.setCompleteDtm(LocalDateTime.now());
    }
}
