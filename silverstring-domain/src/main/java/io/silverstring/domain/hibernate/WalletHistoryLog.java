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
@Table(name="`WalletHistoryLog`")
public class WalletHistoryLog implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String coinName;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal usingBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal addUsingBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal subUsingBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal availableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal addAvailableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal subAvailableBalance;

    private String transId;
    private String transType;
    private Long orderId;
    private String orderType;
    private String requestTxid;
    private String memo;
    private LocalDateTime regDt;
}
