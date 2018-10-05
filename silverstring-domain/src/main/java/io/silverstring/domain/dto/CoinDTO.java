package io.silverstring.domain.dto;

import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.hibernate.Coin;
import lombok.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.List;

public class CoinDTO {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResInfo {
        private List<Coin> infos;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReqCoinAvgPrice {
        private CoinEnum coin;
        private CoinEnum baseCoin;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResCoinAvgPrice {
        private Coin baseCoin;
        private Coin coinInfo;
        private CoinEnum coin;
        private Float totalTradeAmount24h;
        private Float price;
        private Float gapPrice;
        private String marker; //-,+,
        private Long changePercent;
    }
}
