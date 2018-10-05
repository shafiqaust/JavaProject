package io.silverstring.core.repository.hibernate;

import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.CoinCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoinCategoryRepository extends JpaRepository<CoinCategory, Long> {
    public List<CoinCategory> findAllByActiveOrderByDisplayPriorityAsc(ActiveEnum active);
    public List<CoinCategory> findAllByBaseCoinAndActiveOrderByDisplayPriorityAsc(Coin baseCoin, ActiveEnum active);
}
