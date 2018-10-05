package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.ActiveEnum;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class CoinCategory implements Serializable {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name="baseCoinName")
    private Coin baseCoin;

    @OneToOne
    @JoinColumn(name="coinName")
    private Coin subCoin;

    @Column(name = "display_priority")
    private Long displayPriority;

    @Enumerated(EnumType.STRING)
    private ActiveEnum active;
}
