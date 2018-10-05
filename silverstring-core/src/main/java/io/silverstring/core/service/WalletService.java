package io.silverstring.core.service;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.core.exception.ExchangeException;
import io.silverstring.core.provider.wallet.SimpleWalletRpcProvider;
import io.silverstring.core.provider.wallet.WalletRpcProviderFactory;
import io.silverstring.core.repository.hibernate.*;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.*;
import io.silverstring.domain.hibernate.*;
import io.silverstring.domain.util.CompareUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WalletService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletHistoryLogRepository walletHistoryLogRepository;
    private final AdminWalletHistoryLogRepository adminWalletHistoryLogRepository;
    private final CoinRepository coinRepository;
    private final WalletRpcProviderFactory walletRpcProviderFactory;
    private final LevelRepository levelRepository;
    private final AdminWalletRepository adminWalletRepository;
    private final EntityManager entityManager;

    @Autowired
    public WalletService(UserRepository userRepository, WalletRepository walletRepository, WalletHistoryLogRepository walletHistoryLogRepository, AdminWalletHistoryLogRepository adminWalletHistoryLogRepository, CoinRepository coinRepository, WalletRpcProviderFactory walletRpcProviderFactory, LevelRepository levelRepository, AdminWalletRepository adminWalletRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletHistoryLogRepository = walletHistoryLogRepository;
        this.adminWalletHistoryLogRepository = adminWalletHistoryLogRepository;
        this.coinRepository = coinRepository;
        this.walletRpcProviderFactory = walletRpcProviderFactory;
        this.levelRepository = levelRepository;
        this.adminWalletRepository = adminWalletRepository;
        this.entityManager = entityManager;
    }

    @HardTransational
    public void add(WalletDTO.ReqAdd request) {

    }

    @HardTransational
    public void edit(WalletDTO.ReqEdit request) {

    }

    @HardTransational
    public void del(WalletDTO.ReqDel request) {

    }

    public boolean isToken(CoinEnum coinEnum) throws Exception {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
        return simpleWalletRpcProvider.isToken();
    }

    @HardTransational
    public String sendTo(CoinEnum coinEnum, Long userId, String email, String fromAddress, String toAddress, BigDecimal amount) throws Exception {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
        return simpleWalletRpcProvider.sendTo(userId, email, fromAddress, toAddress, amount.doubleValue());
    }

    @HardTransational
    public String sendFromHotWallet(CoinEnum coinEnum, String toAddress, BigDecimal amount) throws Exception {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
        return simpleWalletRpcProvider.sendFromHotWallet(toAddress, amount.doubleValue());
    }

    public Wallet get(Long userId, Coin coin) {
        Optional<Wallet> walletOptional = walletRepository.findOneByUserIdAndCoin(userId, coin);
        if (!walletOptional.isPresent()) {
            throw new ExchangeException(CodeEnum.WALLET_NOT_EXIST);
        }

        return walletOptional.get();
    }

    public WalletDTO.ResWallet getAll(int pageNo, int pageSize) {
        WalletDTO.ResWallet res = new WalletDTO.ResWallet();
        res.setPageNo(pageNo);
        res.setPageSize(pageSize);

        Page<Wallet> results = walletRepository.findAllByOrderByRegDtDesc(new PageRequest(pageNo, pageSize));
        if (results.getContent().size() <= 0) {
            res.setContents(new ArrayList<>());
            res.setPageTotalCnt(results.getTotalPages());
            return res;
        }

        res.setContents(results.getContent());
        res.setPageTotalCnt(results.getTotalPages());

        return res;
    }

    public List<Wallet> getAll(Long userId) {
        return walletRepository.findAllByUserId(userId);
    }

    public WalletDTO.WalletInfos getMyWallets(Long userId, LevelEnum levelEnum, boolean activeFlag) {
        List<WalletDTO.WalletInfos.Info> infos = new ArrayList<>();
        for (CoinEnum coinEnum : CoinEnum.values()) {
            Coin coin = coinRepository.findOne(coinEnum);//코인정보

            if(activeFlag) {
                if ((coin == null) || ActiveEnum.N.equals(coin.getActive())) {
                    continue;
                }
            }

            Level level = levelRepository.findOneByCoinNameAndLevel(coinEnum, levelEnum);// todo uncomment!
            /*Level level = new Level();
            level.setActive(ActiveEnum.Y);
            level.setCoinName(coin.getName());
            level.setOnceAmount(BigDecimal.valueOf(0.1));
            level.setLevel(LevelEnum.LEVEL1);
            level.setOnedayAmount(BigDecimal.valueOf(0.1));*/


            Wallet myWallet = walletRepository.findOneByCoinAndUserId(coin, userId);
            BigDecimal withdrawalFee = getWithdrawalFee(coinEnum);
            BigDecimal realWithdrawalAmount = myWallet.getAvailableBalance().subtract(coin.getWithdrawalMinAmount());

            if (myWallet == null) {
                infos.add(
                        WalletDTO.WalletInfos.Info.builder()
                                .coin(coin)
                                .wallet(null)
                                .level(level)
                                .realWithdrawalAmount(new BigDecimal(0))
                                .withdrawalFee(new BigDecimal(0))
                                .build()
                );
            } else {
                infos.add(
                        WalletDTO.WalletInfos.Info.builder()
                                .coin(coin)
                                .wallet(myWallet)
                                .level(level)
                                .realWithdrawalAmount(realWithdrawalAmount)
                                .withdrawalFee(withdrawalFee)
                                .build()
                );
            }
        }

        return WalletDTO.WalletInfos.builder().infos(infos).build();
    }

    public BigDecimal getTransferFee(CoinEnum coinEnum) {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);

        Coin coin = coinRepository.findOne(coinEnum);
        if(coinEnum.name().equals(CoinEnum.ETHEREUM.name()) || simpleWalletRpcProvider.isToken()) {
            return Convert.fromWei(coin.getWithdrawalFeeGasprice().multiply(coin.getWithdrawalFeeGaslimit()), Convert.Unit.ETHER);
        } else {
            return coin.getWithdrawalFeeAmount();
        }
    }

    public BigDecimal getWithdrawalFee(CoinEnum coinEnum) {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);

        Coin coin = coinRepository.findOne(coinEnum);
        if(coinEnum.name().equals(CoinEnum.ETHEREUM.name()) || simpleWalletRpcProvider.isToken()) {
            if(simpleWalletRpcProvider.isToken()) {
                return coin.getWithdrawalFeeAmount();
            } else {
                return Convert.fromWei(coin.getWithdrawalFeeGasprice().multiply(coin.getWithdrawalFeeGaslimit()), Convert.Unit.ETHER);
            }
        } else {
            return coin.getWithdrawalFeeAmount();
        }
    }

    public BigDecimal getRealWalletTotalBalance(CoinEnum coinEnum, Long userId) {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
        return simpleWalletRpcProvider.getRealBalance(userId);
    }

    public String getUserEthereumPassword(Long userid, String email) {
        SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(CoinEnum.ETHEREUM);
        return simpleWalletRpcProvider.getPassword(userid, email);
    }

    @HardTransational
    public AdminWallet increaseGasAdminBalance(CoinEnum coinEnum, WalletType walletType, BigDecimal amount, String transId, String transType, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        AdminWallet wallet = adminWalletRepository.findOneByCoinNameAndType(coinEnum, walletType).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> increaseGasAdminBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getGasBalance() : " + wallet.getGasBalance().toPlainString());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getGasBalance().add(amount) : " + WalletUtil.scale(wallet.getGasBalance().add(amount)));

        wallet.setGasBalance(WalletUtil.scale(wallet.getGasBalance().add(amount)));

        AdminWalletHistoryLog adminWalletHistoryLog = new AdminWalletHistoryLog();
        adminWalletHistoryLog.setCoinName(coinEnum.name());
        adminWalletHistoryLog.setType(walletType.name());
        adminWalletHistoryLog.setGasBalance(wallet.getGasBalance());
        adminWalletHistoryLog.setAddGasBalance(amount);
        adminWalletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        adminWalletHistoryLog.setTradeFeeBalance(wallet.getTradeFeeBalance());
        adminWalletHistoryLog.setTransId(transId);
        adminWalletHistoryLog.setTransType(transType);
        adminWalletHistoryLog.setMemo(memo);
        adminWalletHistoryLog.setRegDt(LocalDateTime.now());
        adminWalletHistoryLogRepository.save(adminWalletHistoryLog);

        return wallet;
    }

    @HardTransational
    public AdminWallet decreaseGasAdminBalance(CoinEnum coinEnum, WalletType walletType, BigDecimal amount, String transId, String transType, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        AdminWallet wallet = adminWalletRepository.findOneByCoinNameAndType(coinEnum, walletType).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> decreaseGasAdminBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getGasBalance() : " + wallet.getGasBalance().toPlainString());
        log.debug(">>>> amount : -" + amount.toPlainString());
        log.debug(">>>> wallet.getGasBalance().subtract(amount) : " + WalletUtil.scale(wallet.getGasBalance().subtract(amount)));

        wallet.setGasBalance(WalletUtil.scale(wallet.getGasBalance().subtract(amount)));

        AdminWalletHistoryLog adminWalletHistoryLog = new AdminWalletHistoryLog();
        adminWalletHistoryLog.setCoinName(coinEnum.name());
        adminWalletHistoryLog.setType(walletType.name());
        adminWalletHistoryLog.setGasBalance(wallet.getGasBalance());
        adminWalletHistoryLog.setSubGasBalance(amount);
        adminWalletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        adminWalletHistoryLog.setTradeFeeBalance(wallet.getTradeFeeBalance());
        adminWalletHistoryLog.setTransId(transId);
        adminWalletHistoryLog.setTransType(transType);
        adminWalletHistoryLog.setMemo(memo);
        adminWalletHistoryLog.setRegDt(LocalDateTime.now());
        adminWalletHistoryLogRepository.save(adminWalletHistoryLog);

        return wallet;
    }

    @HardTransational
    public AdminWallet increaseAvailableAdminBalance(CoinEnum coinEnum, WalletType walletType, BigDecimal amount, String transId, String transType, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        AdminWallet wallet = adminWalletRepository.findOneByCoinNameAndType(coinEnum, walletType).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> increaseAvailableAdminBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getAvailableBalance() : " + wallet.getAvailableBalance().toPlainString());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getAvailableBalance().add(amount) : " + WalletUtil.scale(wallet.getAvailableBalance().add(amount)));

        wallet.setAvailableBalance(WalletUtil.scale(wallet.getAvailableBalance().add(amount)));

        AdminWalletHistoryLog adminWalletHistoryLog = new AdminWalletHistoryLog();
        adminWalletHistoryLog.setCoinName(coinEnum.name());
        adminWalletHistoryLog.setType(walletType.name());
        adminWalletHistoryLog.setGasBalance(wallet.getGasBalance());
        adminWalletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        adminWalletHistoryLog.setAddAvailableBalance(amount);
        adminWalletHistoryLog.setTradeFeeBalance(wallet.getTradeFeeBalance());
        adminWalletHistoryLog.setTransId(transId);
        adminWalletHistoryLog.setTransType(transType);
        adminWalletHistoryLog.setMemo(memo);
        adminWalletHistoryLog.setRegDt(LocalDateTime.now());
        adminWalletHistoryLogRepository.save(adminWalletHistoryLog);

        return wallet;
    }

    @HardTransational
    public AdminWallet decreaseAvailableAdminBalance(CoinEnum coinEnum, WalletType walletType, BigDecimal amount, String transId, String transType, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        AdminWallet wallet = adminWalletRepository.findOneByCoinNameAndType(coinEnum, walletType).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> decreaseAvailableAdminBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getAvailableBalance() : " + wallet.getAvailableBalance().toPlainString());
        log.debug(">>>> amount : -" + amount.toPlainString());
        log.debug(">>>> wallet.getAvailableBalance().subtract(amount) : " + WalletUtil.scale(wallet.getAvailableBalance().subtract(amount)));

        wallet.setAvailableBalance(WalletUtil.scale(wallet.getAvailableBalance().subtract(amount)));

        AdminWalletHistoryLog adminWalletHistoryLog = new AdminWalletHistoryLog();
        adminWalletHistoryLog.setCoinName(coinEnum.name());
        adminWalletHistoryLog.setType(walletType.name());
        adminWalletHistoryLog.setGasBalance(wallet.getGasBalance());
        adminWalletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        adminWalletHistoryLog.setSubAvailableBalance(amount);
        adminWalletHistoryLog.setTradeFeeBalance(wallet.getTradeFeeBalance());
        adminWalletHistoryLog.setTransId(transId);
        adminWalletHistoryLog.setTransType(transType);
        adminWalletHistoryLog.setMemo(memo);
        adminWalletHistoryLog.setRegDt(LocalDateTime.now());
        adminWalletHistoryLogRepository.save(adminWalletHistoryLog);

        return wallet;
    }

    @HardTransational
    public AdminWallet increaseTradeFeeAdminBalance(CoinEnum coinEnum, WalletType walletType, BigDecimal amount, Long orderId, String orderType, String requestTxid, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        AdminWallet wallet = adminWalletRepository.findOneByCoinNameAndType(coinEnum, walletType).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> increaseTradeFeeAdminBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getTradeFeeBalance() : " + wallet.getTradeFeeBalance().toPlainString());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getTradeFeeBalance().add(amount) : " + WalletUtil.scale(wallet.getTradeFeeBalance().add(amount)));

        wallet.setTradeFeeBalance(WalletUtil.scale(wallet.getTradeFeeBalance().add(amount)));

        AdminWalletHistoryLog adminWalletHistoryLog = new AdminWalletHistoryLog();
        adminWalletHistoryLog.setCoinName(coinEnum.name());
        adminWalletHistoryLog.setType(walletType.name());
        adminWalletHistoryLog.setGasBalance(wallet.getGasBalance());
        adminWalletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        adminWalletHistoryLog.setTradeFeeBalance(wallet.getTradeFeeBalance());
        adminWalletHistoryLog.setAddTradeFeeBalance (amount);
        adminWalletHistoryLog.setOrderId(orderId);
        adminWalletHistoryLog.setOrderType(orderType);
        adminWalletHistoryLog.setRequestTxid(requestTxid);
        adminWalletHistoryLog.setMemo(memo);
        adminWalletHistoryLog.setRegDt(LocalDateTime.now());
        adminWalletHistoryLogRepository.save(adminWalletHistoryLog);

        return wallet;
    }

    @HardTransational
    public void increaseAvailableBalance(Long userId, CoinEnum coinEnum, BigDecimal amount, String transId, String transType, Long orderId, String orderType, String requestTxid, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));
        //Wallet wallet = walletRepository.findOneByCoinAndUserId(new Coin(coinEnum.name()), userId);

        log.debug(">>>> increaseAvailableBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getAvailableBalance() : " + wallet.getAvailableBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getAvailableBalance().add(amount) : " + WalletUtil.scale(wallet.getAvailableBalance().add(amount)));

        wallet.setAvailableBalance(WalletUtil.scale(wallet.getAvailableBalance().add(amount)));

        WalletHistoryLog walletHistoryLog = new WalletHistoryLog();
        walletHistoryLog.setUserId(userId);
        walletHistoryLog.setCoinName(coinEnum.name());
        walletHistoryLog.setUsingBalance(wallet.getUsingBalance());
        walletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        walletHistoryLog.setAddAvailableBalance(amount);
        walletHistoryLog.setTransId(transId);
        walletHistoryLog.setTransType(transType);
        walletHistoryLog.setOrderId(orderId);
        walletHistoryLog.setOrderType(orderType);
        walletHistoryLog.setRequestTxid(requestTxid);
        walletHistoryLog.setMemo(memo);
        walletHistoryLog.setRegDt(LocalDateTime.now());
        walletHistoryLogRepository.save(walletHistoryLog);
    }

    @HardTransational
    public void decreaseAvailableBalance(Long userId, CoinEnum coinEnum, BigDecimal amount, String transId, String transType, Long orderId, String orderType, String requestTxid, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);

        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(wallet.getTotalBalance().subtract(amount)))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        log.debug(">>>> decreaseAvailableBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getAvailableBalance() : " + wallet.getAvailableBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : -" + amount.toPlainString());
        log.debug(">>>> wallet.getAvailableBalance().subtract(amount) : " + WalletUtil.scale(wallet.getAvailableBalance().subtract(amount)));

        wallet.setAvailableBalance(WalletUtil.scale(wallet.getAvailableBalance().subtract(amount)));

        WalletHistoryLog walletHistoryLog = new WalletHistoryLog();
        walletHistoryLog.setUserId(userId);
        walletHistoryLog.setCoinName(coinEnum.name());
        walletHistoryLog.setUsingBalance(wallet.getUsingBalance());
        walletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        walletHistoryLog.setSubAvailableBalance(amount);
        walletHistoryLog.setTransId(transId);
        walletHistoryLog.setTransType(transType);
        walletHistoryLog.setOrderId(orderId);
        walletHistoryLog.setOrderType(orderType);
        walletHistoryLog.setRequestTxid(requestTxid);
        walletHistoryLog.setMemo(memo);
        walletHistoryLog.setRegDt(LocalDateTime.now());
        walletHistoryLogRepository.save(walletHistoryLog);

        log.debug(">>>> decreaseAvailableBalance >>>>>>>>>>>>>end >>>>>>>> ");
    }

    @HardTransational
    public void increaseUsingBalance(Long userId, CoinEnum coinEnum, BigDecimal amount, String transId, String transType, Long orderId, String orderType, String requestTxid, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> increaseUsingBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getUsingBalance() : " + wallet.getUsingBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getUsingBalance().add(amount) : " + WalletUtil.scale(wallet.getUsingBalance().add(amount)));

        wallet.setUsingBalance(WalletUtil.scale(wallet.getUsingBalance().add(amount)));

        WalletHistoryLog walletHistoryLog = new WalletHistoryLog();
        walletHistoryLog.setUserId(userId);
        walletHistoryLog.setCoinName(coinEnum.name());
        walletHistoryLog.setUsingBalance(wallet.getUsingBalance());
        walletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        walletHistoryLog.setAddUsingBalance(amount);
        walletHistoryLog.setTransId(transId);
        walletHistoryLog.setTransType(transType);
        walletHistoryLog.setOrderId(orderId);
        walletHistoryLog.setOrderType(orderType);
        walletHistoryLog.setRequestTxid(requestTxid);
        walletHistoryLog.setMemo(memo);
        walletHistoryLog.setRegDt(LocalDateTime.now());
        walletHistoryLogRepository.save(walletHistoryLog);
    }

    @HardTransational
    public void decreaseUsingBalance(Long userId, CoinEnum coinEnum, BigDecimal amount, String transId, String transType, Long orderId, String orderType, String requestTxid, String memo) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(wallet.getUsingBalance().subtract(amount)))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        log.debug(">>>> decreaseUsingBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getUsingBalance() : " + wallet.getUsingBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : -" + amount.toPlainString());
        log.debug(">>>> wallet.getUsingBalance().subtract(amount) : " + WalletUtil.scale(wallet.getUsingBalance().subtract(amount)));

        wallet.setUsingBalance(WalletUtil.scale(wallet.getUsingBalance().subtract(amount)));

        WalletHistoryLog walletHistoryLog = new WalletHistoryLog();
        walletHistoryLog.setUserId(userId);
        walletHistoryLog.setCoinName(coinEnum.name());
        walletHistoryLog.setUsingBalance(wallet.getUsingBalance());
        walletHistoryLog.setAvailableBalance(wallet.getAvailableBalance());
        walletHistoryLog.setSubUsingBalance(amount);
        walletHistoryLog.setTransId(transId);
        walletHistoryLog.setTransType(transType);
        walletHistoryLog.setOrderId(orderId);
        walletHistoryLog.setOrderType(orderType);
        walletHistoryLog.setRequestTxid(requestTxid);
        walletHistoryLog.setMemo(memo);
        walletHistoryLog.setRegDt(LocalDateTime.now());
        walletHistoryLogRepository.save(walletHistoryLog);
    }

    @HardTransational
    public void increaseTodayWithdrawalTotalBalance(Long userId, CoinEnum coinEnum, BigDecimal amount) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        log.debug(">>>> increaseTodayWithdrawalTotalBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getTodayWithdrawalTotalBalance() : " + wallet.getTodayWithdrawalTotalBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getTodayWithdrawalTotalBalance().add(amount) : " + WalletUtil.scale(wallet.getTodayWithdrawalTotalBalance().add(amount)));

        wallet.setTodayWithdrawalTotalBalance(WalletUtil.scale(wallet.getTodayWithdrawalTotalBalance().add(amount)));
    }

    @HardTransational
    public void decreaseTodayWithdrawalTotalBalance(Long userId, CoinEnum coinEnum, BigDecimal amount) {
        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(amount))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        amount = WalletUtil.scale(amount);
        Wallet wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum.name())).orElseThrow(() -> new ExchangeException(CodeEnum.WALLET_NOT_EXIST));

        if (CompareUtil.Condition.LT.equals(CompareUtil.compareToZero(wallet.getTodayWithdrawalTotalBalance().subtract(amount)))) {
            throw new ExchangeException(CodeEnum.AMOUNT_IS_UNDER_ZERO);
        }

        log.debug(">>>> decreaseTodayWithdrawalTotalBalance >>>>>>>>>>>>>>>>>>>>> ");
        log.debug(">>>> coinEnum.name() : " + coinEnum.name());
        log.debug(">>>> wallet.getTodayWithdrawalTotalBalance() : " + wallet.getTodayWithdrawalTotalBalance().toPlainString() + ", user_id : " + wallet.getUserId());
        log.debug(">>>> amount : +" + amount.toPlainString());
        log.debug(">>>> wallet.getTodayWithdrawalTotalBalance().subtract(amount) : " + WalletUtil.scale(wallet.getTodayWithdrawalTotalBalance().subtract(amount)));

        wallet.setTodayWithdrawalTotalBalance(WalletUtil.scale(wallet.getTodayWithdrawalTotalBalance().subtract(amount)));
    }

    @SoftTransational
    public WalletDTO.ResCreateWallet precreateWallet(Long userId, CoinEnum coinEnum) {
        Optional<Wallet> existWalletOp = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum));
        if (!existWalletOp.isPresent()) {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setAddress(null);
            wallet.setBankCode(null);
            wallet.setBankName(null);
            wallet.setRecvCorpNm(null);
            wallet.setTag(null);
            wallet.setRegDt(LocalDateTime.now());
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setUsingBalance(BigDecimal.ZERO);
            wallet.setTodayWithdrawalTotalBalance(BigDecimal.ZERO);
            wallet.setCoin(new Coin(coinEnum));

            walletRepository.save(wallet);
            entityManager.flush();
            entityManager.clear();
        }

        return WalletDTO.ResCreateWallet.builder().address(null).tag(null).build();
    }

    @SoftTransational
    public WalletDTO.ResCreateWallet createWallet(Long userId, CoinEnum coinEnum) {
        User user = userRepository.findOne(userId);
        Optional<Wallet> existWalletOp = walletRepository.findOneByUserIdAndCoin(userId, new Coin(coinEnum));
        if (existWalletOp.isPresent()) {
            if (StringUtils.isEmpty(existWalletOp.get().getAddress())) {
                SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
                WalletDTO.WalletCreateInfo newAddress = simpleWalletRpcProvider.createWallet(userId, user.getEmail());

                Wallet wallet = existWalletOp.get();
                wallet.setUserId(userId);
                wallet.setAddress(newAddress.getAddress());
                wallet.setBankCode(newAddress.getBankCode());
                wallet.setBankName(newAddress.getBankName());
                wallet.setRecvCorpNm(newAddress.getRecvCorpNm());
                wallet.setTag(newAddress.getTag());
                wallet.setRegDt(LocalDateTime.now());
                wallet.setCoin(new Coin(coinEnum));

                return WalletDTO.ResCreateWallet.builder().address(wallet.getAddress()).tag(wallet.getTag()).build();
            }
            throw new ExchangeException(CodeEnum.WALLET_ALREADY_EXIST);
        } else {
            SimpleWalletRpcProvider simpleWalletRpcProvider = walletRpcProviderFactory.get(coinEnum);
            WalletDTO.WalletCreateInfo newAddress = simpleWalletRpcProvider.createWallet(userId, user.getEmail());

            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setAddress(newAddress.getAddress());
            wallet.setBankCode(newAddress.getBankCode());
            wallet.setBankName(newAddress.getBankName());
            wallet.setRecvCorpNm(newAddress.getRecvCorpNm());
            wallet.setTag(newAddress.getTag());
            wallet.setRegDt(LocalDateTime.now());
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setUsingBalance(BigDecimal.ZERO);
            wallet.setTodayWithdrawalTotalBalance(BigDecimal.ZERO);
            wallet.setCoin(new Coin(coinEnum));
            walletRepository.save(wallet);

            return WalletDTO.ResCreateWallet.builder().address(wallet.getAddress()).tag(wallet.getTag()).build();
        }
    }
}
