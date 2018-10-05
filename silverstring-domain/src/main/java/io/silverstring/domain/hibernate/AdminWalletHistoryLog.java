package io.silverstring.domain.hibernate;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="`AdminWalletHistoryLog`")
public class AdminWalletHistoryLog implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    private String coinName;
    private String type;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal gasBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal addGasBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal subGasBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal availableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal addAvailableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal subAvailableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal tradeFeeBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal addTradeFeeBalance;

    private String transId;
    private String transType;
    private Long orderId;
    private String orderType;
    private String requestTxid;
    private String memo;
    private LocalDateTime regDt;
}
