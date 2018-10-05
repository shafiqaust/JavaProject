package io.silverstring.core.provider.wallet;

import io.silverstring.core.exception.ExchangeException;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.CoinEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletRpcProviderFactory {
    final BitcoinWalletRpcProvider bitcoinRpcProvider;
    final EthereumWalletRpcProvider ethereumWalletRpcProvider;
    final BittrustWalletRpcProvider bittrustWalletRpcProvider;

    @Autowired
    public WalletRpcProviderFactory(BitcoinWalletRpcProvider bitcoinRpcProvider, EthereumWalletRpcProvider ethereumWalletRpcProvider, BittrustWalletRpcProvider bittrustWalletRpcProvider) {
        this.bitcoinRpcProvider = bitcoinRpcProvider;
        this.ethereumWalletRpcProvider = ethereumWalletRpcProvider;
        this.bittrustWalletRpcProvider = bittrustWalletRpcProvider;
    }

    public SimpleWalletRpcProvider get(CoinEnum coinEnum) {
        if (CoinEnum.BITCOIN.equals(coinEnum)) {
            return bitcoinRpcProvider;
        } else if (CoinEnum.ETHEREUM.equals(coinEnum)) {
            return ethereumWalletRpcProvider;
        }  else if (CoinEnum.BITTRUST.equals(coinEnum)) {
            return bittrustWalletRpcProvider;
        } else {
            throw new ExchangeException(CodeEnum.BAD_REQUEST);
        }
    }
}
