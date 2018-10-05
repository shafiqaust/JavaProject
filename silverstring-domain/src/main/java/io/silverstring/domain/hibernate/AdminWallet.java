package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.WalletType;
import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@IdClass(AdminWalletPK.class)
public class AdminWallet implements Serializable {
    @Id
    @Enumerated(EnumType.STRING)
    private CoinEnum coinName;

    @Id
    @Enumerated(EnumType.STRING)
    private WalletType type;

    private String address;
    private String tag;

    private String bankName;
    private String bankCode;
    private String recvCorpNm;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal gasBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal availableBalance;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal tradeFeeBalance;

    private LocalDateTime regDt;
}
