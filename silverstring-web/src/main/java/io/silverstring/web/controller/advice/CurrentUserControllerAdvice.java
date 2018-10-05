package io.silverstring.web.controller.advice;

import io.silverstring.core.repository.hibernate.UserRepository;
import io.silverstring.core.service.CoinService;
import io.silverstring.domain.dto.CoinDTO;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.CoinCategory;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice(basePackages = "io.silverstring.web.controller")
@Order(1)
public class CurrentUserControllerAdvice {
    final UserRepository userRepository;
    final CoinService coinService;
    final Environment environment;

    @Autowired
    public CurrentUserControllerAdvice(UserRepository userRepository, CoinService coinService, Environment environment) {
        this.userRepository = userRepository;
        this.coinService = coinService;
        this.environment = environment;
    }

    @ModelAttribute("user")
    public User getCurrentUser(Authentication authentication) {
        return (authentication == null) ? null : ((User) authentication.getPrincipal());
    }

    @ModelAttribute("loginStatus")
    public boolean getLoginStatus(Authentication authentication) {
        return (authentication == null) ? false : true;
    }

    @ModelAttribute("version")
    public long getVersion() {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        return timestamp.getTime();
    }

    @ModelAttribute("localeName")
    public String getLocaleName() {
        return LocaleContextHolder.getLocale().getLanguage().substring(0, 2);
    }

    @ModelAttribute("baseurl")
    public String getBaseUrl() {
        return System.getProperty("base.url");
    }

    @ModelAttribute("coins")
    public CoinDTO.ResInfo getCoins(HttpServletRequest request) {
        if(request.getRequestURI().indexOf("coinmartketcap.com") > 0) {
            return null;
        } else {
            return coinService.getActiveCoins();
        }
    }

    @ModelAttribute("baseCoin")
    public Coin getBaseCoin(HttpServletRequest request) {
        if(request.getRequestURI().indexOf("coinmartketcap.com") > 0) {
            return null;
        } else {
            return coinService.getBaseCoin();
        }
    }

    @ModelAttribute("categoriedCoins")
    public Map<Coin, List<CoinCategory>> getCategoriedCoins(HttpServletRequest request) {
        if(request.getRequestURI().indexOf("coinmartketcap.com") > 0) {
            return null;
        } else {
            return coinService.getCoinCategories(null);
        }
    }
}
