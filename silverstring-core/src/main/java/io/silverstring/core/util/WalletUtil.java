package io.silverstring.core.util;

import io.silverstring.domain.enums.CategoryEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WalletUtil {
    @Value("${wallet.address.prefix}")
    String ADDRESS_PREFIX;

    public static List<Map<String, Object>> filterCoinTransactionCategory(List<Map<String, Object>> transactions, CategoryEnum category) {
        if (transactions == null || transactions.size() <= 0) {
            return new ArrayList<>();
        }

        return transactions.stream().filter(t -> {
            if (category.name().equals(t.get("category"))) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static BigDecimal scale(BigDecimal target) {
        return target.setScale(8, RoundingMode.DOWN);
    }

    public String getAddressPrefix() {
        return ADDRESS_PREFIX;
    }
}
