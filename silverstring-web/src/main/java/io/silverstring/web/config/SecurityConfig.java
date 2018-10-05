package io.silverstring.web.config;

import io.silverstring.core.handler.auth.CustomAuthenticationFailureHandler;
import io.silverstring.core.handler.auth.CustomAuthenticationSuccessHandler;
import io.silverstring.core.service.login.CustomAuthenticationProvider;
import io.silverstring.core.service.login.CustomWebAuthenticationDetailsSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Slf4j
@Configuration
@EnableRedisHttpSession(redisNamespace = "${spring.redis.namespace}")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomWebAuthenticationDetailsSource authenticationDetailsSource;

    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String serverDomain = SECURITY_REQUEST_SSL ? System.getProperty("base.url") : "";

        http
                .authorizeRequests()
                    .antMatchers("/api/common/**", "/user/doRegist", "/term", "/regist", "/user/doUserSearch", "/userSearch", "/user/doEmailSearch", "/emailSearch", "/emailConfirm", "/login", "/faq_manage", "/assets/**","/static/**",  "/", "/public/**", "/doc/**", "/img/**", "/css/**", "/fonts/**", "/js/**", "/less/**", "/sass/**", "/vendor/**", "/font-awesome/**").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                .formLogin()
                    .loginPage(serverDomain + "/login")
                    .failureUrl(serverDomain + "/login")
                    .failureHandler(authenticationFailureHandler())
                    .successHandler(authenticationSuccessHandler())
                    .loginProcessingUrl(serverDomain + "/j_spring_security_check")
                    .defaultSuccessUrl(serverDomain + "/deposit_manage", true)
                    .usernameParameter("email")
                    .passwordParameter("pwd")
                    .authenticationDetailsSource(authenticationDetailsSource)
                    .permitAll()
                .and()
                    .logout()
                    .logoutUrl(serverDomain + "/logout")
                    .deleteCookies("remember-me")
                    .logoutSuccessUrl(serverDomain + "/")
                    .invalidateHttpSession(true)
                    .permitAll()
                .and()
                    .rememberMe()
                .and()
                    //.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                    //.csrf().csrfTokenRepository(csrfTokenRepository()).ignoringAntMatchers("/api/test/**", "/api/common/**");
                    .csrf().csrfTokenRepository(csrfTokenRepository());
                    //.csrf().csrfTokenRepository(csrfTokenRepository()).disable();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        //auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
        auth.authenticationProvider(authProvider());
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
