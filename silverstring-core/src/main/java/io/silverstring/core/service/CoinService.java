package io.silverstring.core.service;

import io.silverstring.core.repository.hibernate.CoinCategoryRepository;
import io.silverstring.core.repository.hibernate.CoinRepository;
import io.silverstring.domain.dto.CoinDTO;
import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.CoinCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CoinService {
    final CoinRepository coinRepository;
    final CoinCategoryRepository coinCategoryRepository;

    @Autowired
    public CoinService(CoinRepository coinRepository, CoinCategoryRepository coinCategoryRepository) {
        this.coinRepository = coinRepository;
        this.coinCategoryRepository = coinCategoryRepository;
    }

    public Map<Coin, List<CoinCategory>> getCoinCategories(CoinEnum coinEnum) {
        List<CoinCategory> coinCategories = null;
        if (coinEnum == null) {
            coinCategories = Optional.ofNullable(coinCategoryRepository.findAllByActiveOrderByDisplayPriorityAsc(ActiveEnum.Y)).orElseGet(ArrayList::new);
        } else {
            coinCategories = Optional.ofNullable(coinCategoryRepository.findAllByBaseCoinAndActiveOrderByDisplayPriorityAsc(new Coin(coinEnum), ActiveEnum.Y)).orElseGet(ArrayList::new);
        }

        Map<Coin, List<CoinCategory>> groupByCoin = groupByCoin = coinCategories.stream().filter(r -> ActiveEnum.Y.equals(r.getActive())).collect(Collectors.groupingBy(CoinCategory::getBaseCoin));
        Map<Coin, List<CoinCategory>> sortedGroupByCoin = new TreeMap<>(
                new Comparator<Coin>() {
                    @Override
                    public int compare(Coin o1, Coin o2) {
                        return o1.getDisplayPriority().compareTo(o2.getDisplayPriority());
                    }
                }
            );
        sortedGroupByCoin.putAll(groupByCoin);

        return sortedGroupByCoin;
    }

    public Coin getCoin(CoinEnum coinEnum) {
        return coinRepository.findOne(coinEnum);
    }

    public Coin getActiveCoin(CoinEnum coinEnum) {
        Coin coin = coinRepository.findOne(coinEnum);
        if (!ActiveEnum.Y.equals(coin.getActive())) {
            log.warn("coin status is deactive.");
            return null;
        }
        return coin;
    }

    public Coin getBaseCoin() {
        Coin coin = coinRepository.findOneByIsBaseCoin(ActiveEnum.Y);
        if (!ActiveEnum.Y.equals(coin.getActive())) {
            log.warn("coin status is deactive.");
            return null;
        }
        return coin;
    }

    public CoinDTO.ResInfo getActiveCoins() {
        List<Coin> coins = coinRepository.findAllByActiveOrderByDisplayPriorityAsc(ActiveEnum.Y);
        return CoinDTO.ResInfo.builder().infos(coins).build();
    }

    public CoinDTO.ResInfo getCoins() {
        List<Coin> coins = coinRepository.findAllByOrderByDisplayPriorityAsc();
        return CoinDTO.ResInfo.builder().infos(coins).build();
    }
}
