package io.silverstring.core.provider.wallet;

import io.silverstring.core.provider.wallet.proxy.BitcoinWalletRpcProxyProvider;
import io.silverstring.core.repository.hibernate.CoinRepository;
import io.silverstring.core.util.WalletUtil;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.WalletType;
import io.silverstring.domain.hibernate.Coin;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BitcoinWalletRpcProvider implements SimpleWalletRpcProvider {
    final BitcoinWalletRpcProxyProvider bitcoinWalletRpcProxyProvider;
    final CoinRepository coinRepository;
    final ModelMapper modelMapper;
    final WalletUtil walletUtil;

    @Value("${wallet.bitcoin.unlock.passphase}")
    String unlockPassphras;

    @Value("${wallet.bitcoin.unlock.timeout}")
    Integer unlockTimeout;

    @Autowired
    public BitcoinWalletRpcProvider(BitcoinWalletRpcProxyProvider bitcoinWalletRpcProxyProvider, CoinRepository coinRepository, ModelMapper modelMapper, WalletUtil walletUtil) {
        this.bitcoinWalletRpcProxyProvider = bitcoinWalletRpcProxyProvider;
        this.coinRepository = coinRepository;
        this.modelMapper = modelMapper;
        this.walletUtil = walletUtil;
    }

    @Override
    public Coin getCoin() {
        return coinRepository.findOne(CoinEnum.BITCOIN);
    }

    @Override
    public boolean isToken() {
        return false;
    }

    @Override
    public BigDecimal getGasLimit() {
        return null;
    }

    @Override
    public BigDecimal getGasPrice() {
        return null;
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
        return null;//사용하지않음
    }

    @Override
    public WalletDTO.WalletCreateInfo createWallet(Long userId, String email) {
        return WalletDTO.WalletCreateInfo.builder().address(bitcoinWalletRpcProxyProvider.getaccountaddress(getAccount(userId))).build();
    }

    @Override
    public BigDecimal getRealBalance(Long userId) {
        return new BigDecimal(String.valueOf(bitcoinWalletRpcProxyProvider.getbalance(getAccount(userId))));
    }

    @Override
    public WalletDTO.TransactionInfo getTransaction(String txid) {
        Map<String, Object> transaction = bitcoinWalletRpcProxyProvider.gettransaction(txid);
        return modelMapper.map(transaction, WalletDTO.TransactionInfo.class);
    }

    @Override
    public List<WalletDTO.TransactionInfo> getTransactions(CategoryEnum categoryEnum, Page page) {
        List<Map<String, Object>> transactions = bitcoinWalletRpcProxyProvider.listtransactions("*", page.getPageSize(), page.getPageNo());

        log.debug(">>>>>>>> " +  CoinEnum.BITCOIN.name() + " search block infomation >>>>>>>>>> [" + page.getPageSize() + "] ~ [1]");

        List<WalletDTO.TransactionInfo> transactionInfos = modelMapper.map(transactions, new TypeToken<List<WalletDTO.TransactionInfo>>(){}.getType());
        return transactionInfos.stream().filter(r -> {
            if (categoryEnum.equals(r.getCategory())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Override
    public String sendTo(Long userId, String email, String fromAddress, String toAddress, Double amount) throws Exception {
        try {
            bitcoinWalletRpcProxyProvider.walletpassphrase(unlockPassphras, unlockTimeout);
            bitcoinWalletRpcProxyProvider.settxfee(getCoin().getWithdrawalFeeAmount().doubleValue());
            return bitcoinWalletRpcProxyProvider.sendfrom(getAccount(userId), toAddress, amount);
        } catch(Exception ex) {
            throw new Exception(ex);
        } finally {
            bitcoinWalletRpcProxyProvider.walletlock();
        }
    }

    @Override
    public String sendFromHotWallet(String toAddress, Double amount) throws Exception {
        try {
            bitcoinWalletRpcProxyProvider.walletpassphrase(unlockPassphras, unlockTimeout);
            bitcoinWalletRpcProxyProvider.settxfee(getCoin().getWithdrawalFeeAmount().doubleValue());
            return bitcoinWalletRpcProxyProvider.sendfrom(WalletType.HOT.name(), toAddress, amount);
        } catch(Exception ex) {
            throw new Exception(ex);
        } finally {
            bitcoinWalletRpcProxyProvider.walletlock();
        }
    }

    @Override
    public String getSendAddressFromTxId(String txid, WalletDTO.TransactionInfo transactionInfo) {
        //TODO 더 효율적으로 구하는 방법을 찾다
        return transactionInfo.getAddress();
    }
}
