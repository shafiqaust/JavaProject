package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.enums.StatusEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@IdClass(ManualTransactionPK.class)
public class ManualTransaction implements Serializable {
    @Id
    private String id;

    @Id
    private Long userId;

    @OneToOne
    @JoinColumn(name="coinName")
    private Coin coin;

    @Enumerated(EnumType.STRING)
    private CategoryEnum category;

    private String address;
    private String tag;
    private String bankNm;
    private String recvNm;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal amount;

    private LocalDateTime regDt;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    private String depositDvcd;
    private String reason;
}
