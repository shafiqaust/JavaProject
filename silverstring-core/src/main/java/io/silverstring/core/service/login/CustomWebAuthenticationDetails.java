package io.silverstring.core.service.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {
    private String otpcode;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        otpcode = request.getParameter("optcode");
    }

    public String getOtpcode() {
        return otpcode;
    }
}
