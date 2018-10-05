package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.enums.CoinEnum;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Coin implements Serializable {
    @Id
    @GeneratedValue
    @Enumerated(EnumType.STRING)
    private CoinEnum name;

    private String hanName;

    private String mark;
    private String unit;
    @Column(name = "display_priority")
    private Long displayPriority;

    private String rpcUrl;
    private String logoUrl;

    private LocalDateTime regDtm;

    @Enumerated(EnumType.STRING)
    private ActiveEnum isBaseCoin;

    @Enumerated(EnumType.STRING)
    private ActiveEnum active;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal withdrawalMinAmount;//최소출금금액

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal withdrawalAutoAllowMaxAmount;//자동출금최대금액

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal withdrawalFeeAmount;//출금수수료

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal withdrawalFeeGaslimit;//가스LIMIT

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal withdrawalFeeGasprice;//가스LIMIT

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal autoCollectMinAmount;//COLD WALLET 자동이체 최소금액

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal tradingFeePercent;//거래수수료

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal tradingMinAmount;//거래최소금액

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal marginTradingFeePercent;

    private Integer depositScanStartOffset;
    private Integer depositScanPageOffset;
    private Integer depositScanPageSize;

    private Long depositAllowConfirmation;
    private Integer icoStakeAmount;

    public Coin() {
    }

    public Coin(CoinEnum coinEnum) {
        this.name = coinEnum;
    }

    public Coin(String coinName) {
        this.name = CoinEnum.valueOf(coinName);
    }
}
