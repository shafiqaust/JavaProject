package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.LevelEnum;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(LevelPK.class)
public class Level implements Serializable {
    @Id
    @Enumerated(EnumType.STRING)
    private CoinEnum coinName;

    @Id
    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal onceAmount;

    @Digits(integer=32, fraction=8)
    @DecimalMin("0.00000000")
    private BigDecimal onedayAmount;

    @Enumerated(EnumType.STRING)
    private ActiveEnum active;
}
