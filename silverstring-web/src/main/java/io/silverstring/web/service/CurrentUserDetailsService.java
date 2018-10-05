package io.silverstring.web.service;

import io.silverstring.core.repository.hibernate.CoinRepository;
import io.silverstring.core.service.ActionLogService;
import io.silverstring.core.service.UserService;
import io.silverstring.core.service.WalletService;
import io.silverstring.domain.dto.CurrentUserDTO;
import io.silverstring.domain.enums.CoinEnum;
import io.silverstring.domain.enums.TagEnum;
import io.silverstring.domain.hibernate.Coin;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.List;

@Slf4j
@Service
public class CurrentUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final ActionLogService actionLogService;
    private final CoinRepository coinRepository;
    private final WalletService walletService;

    @Autowired
    public CurrentUserDetailsService(UserService userService, ActionLogService actionLogService, CoinRepository coinRepository, WalletService walletService) {
        this.userService = userService;
        this.actionLogService = actionLogService;
        this.coinRepository = coinRepository;
        this.walletService = walletService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getActiveUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("User with email=%s was not found", email)));

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        List<Coin> coins = coinRepository.findAll();
        for (Coin coin : coins) {
            if(coin.getName().equals(CoinEnum.BITCOIN))  //removing BTC for now
            {
                continue;
            }
            walletService.precreateWallet(user.getId(), coin.getName());
        }

        actionLogService.log(user.getId(), TagEnum.LOGIN);

        return new CurrentUserDTO(user);
    }
}
