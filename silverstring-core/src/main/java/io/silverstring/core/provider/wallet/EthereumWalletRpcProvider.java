package io.silverstring.core.provider.wallet;

import io.silverstring.core.exception.ExchangeException;
import io.silverstring.core.provider.util.erc20.Erc20ContractUtil;
import io.silverstring.core.provider.util.erc20.HumanStandardToken;
import io.silverstring.core.provider.wallet.proxy.EthereumWalletRpcProxyProvider;
import io.silverstring.core.repository.hibernate.AdminWalletRepository;
import io.silverstring.core.repository.hibernate.CoinRepository;
import io.silverstring.core.repository.hibernate.UserRepository;
import io.silverstring.core.repository.hibernate.WalletRepository;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.WalletType;
import io.silverstring.domain.hibernate.AdminWallet;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.User;
import io.silverstring.domain.hibernate.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.observables.BlockingObservable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

@Slf4j
@Service
public class EthereumWalletRpcProvider implements SimpleWalletRpcProvider {
    final Web3j ethereumWalletWeb3jProxyProvider;
    final Quorum ethereumWalletQuorumProxyProvider;
    final EthereumWalletRpcProxyProvider ethereumWalletRpcProxyProvider;
    final CoinRepository coinRepository;
    final AdminWalletRepository adminWalletRepository;
    final WalletRepository walletRepository;
    final ModelMapper modelMapper;
    final WalletUtil walletUtil;
    final Erc20ContractUtil erc20ContractUtil;

    @Value("${wallet.ethereum.unlock.passphase}")
    String unlockPassphras;

    @Value("${wallet.ethereum.unlock.timeout}")
    Integer unlockTimeout;

    @Autowired
    public EthereumWalletRpcProvider(Web3j ethereumWalletWeb3jProxyProvider, Quorum ethereumWalletQuorumProxyProvider, EthereumWalletRpcProxyProvider ethereumWalletRpcProxyProvider, CoinRepository coinRepository, AdminWalletRepository adminWalletRepository, WalletRepository walletRepository, ModelMapper modelMapper, WalletUtil walletUtil, Erc20ContractUtil erc20ContractUtil) {
        this.ethereumWalletWeb3jProxyProvider = ethereumWalletWeb3jProxyProvider;
        this.ethereumWalletQuorumProxyProvider = ethereumWalletQuorumProxyProvider;
        this.ethereumWalletRpcProxyProvider = ethereumWalletRpcProxyProvider;
        this.coinRepository = coinRepository;
        this.adminWalletRepository = adminWalletRepository;
        this.walletRepository = walletRepository;
        this.modelMapper = modelMapper;
        this.walletUtil = walletUtil;
        this.erc20ContractUtil = erc20ContractUtil;
    }

    @Override
    public Coin getCoin() {
        return coinRepository.findOne(CoinEnum.ETHEREUM);
    }

    @Override
    public boolean isToken() {
        return false;
    }

    @Override
    public BigDecimal getGasLimit() {
        return getCoin().getWithdrawalFeeGaslimit();
    }

    @Override
    public BigDecimal getGasPrice() {
        return getCoin().getWithdrawalFeeGasprice();
    }

    @Override
    public BigDecimal getWithdrawalFee()  {
        return getCoin().getWithdrawalFeeAmount();
    }

    @Override
    public String getAccount(Long userId) {
        return walletUtil.getAddressPrefix() + String.valueOf(userId);
    }

    @Override
    public String getPassword(Long userId, String email) {
        return DigestUtils.md5Hex(getAccount(userId) + email);
    }

    @Override
    public WalletDTO.WalletCreateInfo createWallet(Long userId, String email) {
        String address = ethereumWalletRpcProxyProvider.personal_newAccount(getPassword(userId, email));//personal_newAccount(password)
        return WalletDTO.WalletCreateInfo.builder().address(address).tag("").build();
    }

    @Override
    public BigDecimal getRealBalance(Long userId) {
        Optional<Wallet> wallet = walletRepository.findOneByUserIdAndCoin(userId, new Coin(getCoin().getName()));
        if (!wallet.isPresent()) {
            throw new ExchangeException(CodeEnum.WALLET_NOT_EXIST);
        }

        String _balance = ethereumWalletRpcProxyProvider.eth_getBalance(wallet.get().getAddress(), "latest").substring(2);
        BigDecimal tmpBalance = new BigDecimal(new BigInteger(_balance, 16));
        String bal = tmpBalance.toString();
        if ("0".equals(bal)) {
            return new BigDecimal("0.0");
        }
        String k = String.format("%019.0f", tmpBalance);

        return new BigDecimal(k.substring(0, k.length()-18) + "." + k.substring(k.length() - 18));
    }

    public BigDecimal getRealTokenBalance(String BINARY, String contractAddress, String address) throws Exception {
        long _amount = erc20ContractUtil.balanceOf(BINARY, contractAddress, address, address, getGasPrice().toBigInteger(), getGasLimit().toBigInteger());
        return new BigDecimal(_amount);
    }

    @Override
    public WalletDTO.TransactionInfo getTransaction(String txid) {
        return null;
    }

    @Override
    public List<WalletDTO.TransactionInfo> getTransactions(CategoryEnum categoryEnum, Page page) throws Exception {
        BigInteger currentBlockNumber = ethereumWalletWeb3jProxyProvider.ethBlockNumber().send().getBlockNumber();
        BigInteger endBlock = BigInteger.valueOf(currentBlockNumber.longValue() - page.getPageNo());
        BigInteger startBlock = BigInteger.valueOf(endBlock.longValue() - page.getPageSize());

        Observable<EthBlock> ethBlockObservable = ethereumWalletWeb3jProxyProvider.replayBlocksObservable(
                DefaultBlockParameter.valueOf(startBlock),
                DefaultBlockParameter.valueOf(endBlock),
                true);

        log.debug(">>>>>>>> " +  CoinEnum.ETHEREUM.name() + " search block infomation >>>>>>>>>> [" + startBlock.longValue() + "] ~ [" + endBlock.longValue() + "]" + "- [" + currentBlockNumber.longValue() + "]");

        Map<String, Wallet> existWalletMap = new HashMap<>();
        List<Wallet> existWallets = walletRepository.findAllByCoin(getCoin());
        for (Wallet existWallet : existWallets) {
            if (!StringUtils.isEmpty(existWallet.getAddress())) {
                existWalletMap.put(existWallet.getAddress(), existWallet);
            }
        }

        List<WalletDTO.TransactionInfo> transactionInfos = new ArrayList<>();
        BlockingObservable.from(ethBlockObservable).subscribe(block -> {
            List<EthBlock.TransactionResult> transactions =  block.getResult().getTransactions();
            for (EthBlock.TransactionResult transaction : transactions) {
                EthBlock.TransactionObject transactionHash = (EthBlock.TransactionObject)transaction.get();

                CategoryEnum ethCategory = CategoryEnum.send;
                if (existWalletMap.containsKey(transactionHash.getTo())) {
                    ethCategory = CategoryEnum.receive;
                }

                if (categoryEnum.equals(ethCategory)) {
                    transactionInfos.add(
                            WalletDTO.TransactionInfo.builder()
                                    .address(transactionHash.getTo())
                                    .amount(Convert.fromWei(new BigDecimal(transactionHash.getValue()), Convert.Unit.ETHER))
                                    .category(ethCategory)
                                    .confirmations(currentBlockNumber.subtract(transactionHash.getBlockNumber()))
                                    //.timereceived(LocalDateTime.now())
                                    .txid(transactionHash.getHash())
                                    .build()
                    );
                }
            }
        });

        return transactionInfos;
    }

    @Override
    public String sendTo(Long userId, String email, String fromAddress, String toAddress, Double amount) throws Exception {
        ethereumWalletRpcProxyProvider.personal_unlockAccount(fromAddress, getPassword(userId, email), new BigInteger(unlockTimeout.toString()));

        return ethereumWalletRpcProxyProvider.eth_sendTransaction(
                EthereumWalletRpcProxyProvider.EthTransaction.builder()
                        .from(fromAddress)
                        .to(toAddress)
                        .gas(String.format("0x%x", getGasLimit().toBigInteger()))
                        .gasPrice(String.format("0x%x", getGasPrice().toBigInteger()))
                        .value(String.format("0x%x", Convert.toWei(new BigDecimal(amount), Convert.Unit.ETHER).toBigInteger()))
                        .data("0x")
                        .build()
        );
    }

    public String sendTokenTo(String BINARY, String contractAddress, Long userId, String email, String fromAddress, String toAddress, Double amount) throws Exception {
        ethereumWalletRpcProxyProvider.personal_unlockAccount(fromAddress, getPassword(userId, email), new BigInteger(unlockTimeout.toString()));

        NoOpProcessor processor = new NoOpProcessor(ethereumWalletQuorumProxyProvider);
        TransactionManager transactionManager = new org.web3j.tx.ClientTransactionManager(ethereumWalletWeb3jProxyProvider, fromAddress, processor);
        HumanStandardToken humanStandardToken = HumanStandardToken.load(BINARY, contractAddress, ethereumWalletWeb3jProxyProvider, transactionManager, getGasPrice().toBigInteger(), getGasLimit().toBigInteger());

        org.web3j.protocol.core.RemoteCall<TransactionReceipt> transactionReceipt = humanStandardToken.transfer(toAddress, Convert.toWei(new BigDecimal(amount), Convert.Unit.ETHER).toBigInteger());

        return transactionReceipt.send().getTransactionHash();
    }

    @Override
    public String sendFromHotWallet(String toAddress, Double amount) throws Exception {
        Optional<AdminWallet> adminWallet = Optional.ofNullable(adminWalletRepository.findOneByCoinNameAndType(getCoin().getName(), WalletType.HOT)).orElseThrow(() -> new ExchangeException(CodeEnum.ADMIN_WALLET_NOT_EXIST));

        ethereumWalletRpcProxyProvider.personal_unlockAccount(adminWallet.get().getAddress(), unlockPassphras, new BigInteger(unlockTimeout.toString()));

        return ethereumWalletRpcProxyProvider.eth_sendTransaction(
                EthereumWalletRpcProxyProvider.EthTransaction.builder()
                        .from(adminWallet.get().getAddress())
                        .to(toAddress)
                        .gas(String.format("0x%x", getGasLimit().toBigInteger()))
                        .gasPrice(String.format("0x%x", getGasPrice().toBigInteger()))
                        .value(String.format("0x%x", Convert.toWei(new BigDecimal(amount), Convert.Unit.ETHER).toBigInteger()))
                        .data("0x")
                        .build()
        );
    }

    public String sendTokenFromHotWallet(String BINARY, String contractAddress, String toAddress, Double amount) throws Exception {
        Optional<AdminWallet> adminWallet = Optional.ofNullable(adminWalletRepository.findOneByCoinNameAndType(getCoin().getName(), WalletType.HOT)).orElseThrow(() -> new ExchangeException(CodeEnum.ADMIN_WALLET_NOT_EXIST));

        ethereumWalletRpcProxyProvider.personal_unlockAccount(adminWallet.get().getAddress(), unlockPassphras, new BigInteger(unlockTimeout.toString()));

        NoOpProcessor processor = new NoOpProcessor(ethereumWalletQuorumProxyProvider);
        TransactionManager transactionManager = new org.web3j.tx.ClientTransactionManager(ethereumWalletWeb3jProxyProvider, adminWallet.get().getAddress(), processor);
        HumanStandardToken humanStandardToken = HumanStandardToken.load(BINARY, contractAddress, ethereumWalletWeb3jProxyProvider, transactionManager, getGasPrice().toBigInteger(), getGasLimit().toBigInteger());

        org.web3j.protocol.core.RemoteCall<TransactionReceipt> transactionReceipt = humanStandardToken.transfer(toAddress, Convert.toWei(new BigDecimal(amount), Convert.Unit.ETHER).toBigInteger());

        return transactionReceipt.send().getTransactionHash();
    }

    @Override
    public String getSendAddressFromTxId(String txid, WalletDTO.TransactionInfo transactionInfo) {
        return transactionInfo.getAddress();
    }

    public List<WalletDTO.TransactionInfo> searchLogs(String BINARY, String contractAddress, CategoryEnum categoryEnum, CoinEnum coinEnum, Page page) throws Exception {
        BigInteger currentBlockNumber = ethereumWalletWeb3jProxyProvider.ethBlockNumber().send().getBlockNumber();
        BigInteger endBlock = BigInteger.valueOf(currentBlockNumber.longValue() - page.getPageNo());
        BigInteger startBlock = BigInteger.valueOf(endBlock.longValue() - page.getPageSize());

        Optional<AdminWallet> adminWallet = Optional.ofNullable(adminWalletRepository.findOneByCoinNameAndType(coinEnum, WalletType.HOT)).orElseThrow(() -> new ExchangeException(CodeEnum.ADMIN_WALLET_NOT_EXIST));
        TransactionManager transactionManager = new org.web3j.quorum.tx.ClientTransactionManager(ethereumWalletQuorumProxyProvider, adminWallet.get().getAddress(), Collections.emptyList());
        HumanStandardToken humanStandardToken = HumanStandardToken.load(BINARY, contractAddress, ethereumWalletQuorumProxyProvider, transactionManager, GAS_PRICE, GAS_LIMIT);
        Observable<HumanStandardToken.TransferEventResponse>  tokenBlockObservable = humanStandardToken.transferEventObservable(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(startBlock.longValue())),
                DefaultBlockParameter.valueOf(BigInteger.valueOf(endBlock.longValue()))
        );

        log.debug(">>>>>>>> " + coinEnum.name() + " search block infomation >>>>>>>>>> [" + startBlock.longValue() + "] ~ [" + endBlock.longValue() + "]" + "- [" + currentBlockNumber.longValue() + "]");

        Map<String, Wallet> existWalletMap = new HashMap<>();
        List<Wallet> existWallets = walletRepository.findAllByCoin(getCoin());
        for (Wallet existWallet : existWallets) {
            if (!StringUtils.isEmpty(existWallet.getAddress())) {
                existWalletMap.put(existWallet.getAddress(), existWallet);
            }
        }

        List<WalletDTO.TransactionInfo> transactionInfos = new ArrayList<>();
        BlockingObservable.from(tokenBlockObservable).subscribe(block -> {
            CategoryEnum ethCategory = CategoryEnum.send;
            if (existWalletMap.containsKey(block._to)) {
                ethCategory = CategoryEnum.receive;
            }

            if (categoryEnum.equals(ethCategory)) {
                transactionInfos.add(
                    WalletDTO.TransactionInfo.builder()
                        .address(block._to)
                        .amount(Convert.fromWei(new BigDecimal(block._value), Convert.Unit.ETHER))
                        .category(ethCategory)
                        .confirmations(currentBlockNumber.subtract(block._blockNumber))
                        //.timereceived(LocalDateTime.now())
                        .txid(block._transactionHash)
                        .build()
                    );
            }
        });

        return transactionInfos;
    }
}