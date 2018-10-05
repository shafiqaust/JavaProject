package io.silverstring.domain.hibernate;

import lombok.Data;
import org.springframework.context.annotation.Lazy;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@IdClass(WalletPK.class)
public class Wallet implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Id
    private Long userId;

    @OneToOne
    @JoinColumn(name="coinName")

    @Lazy
    private Coin coin;

    private String address;
    private String bankName;
    private String bankCode;
    private String recvCorpNm;
    private String tag;
    private String depositDvcd;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal usingBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal availableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal todayWithdrawalTotalBalance;

    private LocalDateTime regDt;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    @Transient
    private String realBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    @Transient
    public BigDecimal getTotalBalance() {
        return usingBalance.add(availableBalance);
    }
}
