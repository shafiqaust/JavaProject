package io.silverstring.core.provider.wallet;

import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.hibernate.Coin;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

public interface SimpleWalletRpcProvider {
    Coin getCoin();
    boolean isToken();
    BigDecimal getGasPrice();
    BigDecimal getGasLimit();
    BigDecimal getWithdrawalFee();
    String getAccount(Long userId);
    String getPassword(Long userId, String email);
    WalletDTO.WalletCreateInfo createWallet(Long userId, String email);
    BigDecimal getRealBalance(Long userId);
    WalletDTO.TransactionInfo getTransaction(String txid);
    List<WalletDTO.TransactionInfo> getTransactions(CategoryEnum categoryEnum, Page page) throws Exception;
    String sendTo(Long userId, String email, String fromAddress, String toAddress, Double amount) throws Exception;
    String sendFromHotWallet(String toAddress, Double amount) throws Exception;
    String getSendAddressFromTxId(String txid, WalletDTO.TransactionInfo transactionInfo);

    @Data
    public static class Page {
        private int pageNo;
        private int pageSize;
    }
}
