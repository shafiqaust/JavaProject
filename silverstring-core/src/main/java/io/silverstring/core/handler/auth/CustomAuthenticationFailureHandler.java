package io.silverstring.core.handler.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if(SECURITY_REQUEST_SSL) {
            response.sendRedirect(System.getProperty("base.url") + "/login?msg=invalid");
        } else {
            response.sendRedirect("/login?msg=invalid");
        }
    }
}
