package io.silverstring.core.service.login;

import io.silverstring.core.repository.hibernate.UserRepository;
import io.silverstring.core.service.OtpService;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.enums.OtpStatus;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String otpCode = ((CustomWebAuthenticationDetails) auth.getDetails()).getOtpcode();

        User user = userRepository.findOneByEmail(auth.getName());

        try {
            if ((user == null)) {
                throw new BadCredentialsException(CodeEnum.USER_NOT_EXIST.getMessage());
            } else {
                if((user.getOtpStatus() != null) && user.getOtpStatus().equals(OtpStatus.C.name())) {
                    if (!otpService.isOtpValid(user, otpCode)) {
                        //throw new ExchangeException(CodeEnum.INVALID_OTP);
                        throw new BadCredentialsException(CodeEnum.INVALID_OTP.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            throw new BadCredentialsException(CodeEnum.FAIL.getMessage());
        }


      //todo
       /* Authentication result = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };*/
        Authentication resAuthentication = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(user, resAuthentication.getCredentials(), resAuthentication.getAuthorities());
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
