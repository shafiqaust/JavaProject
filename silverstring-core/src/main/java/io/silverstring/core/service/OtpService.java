package io.silverstring.core.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.KeyRepresentation;
import io.silverstring.domain.enums.OtpStatus;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OtpService {
    private GoogleAuthenticator googleAuthenticator;

    @PostConstruct
    public void init() {
        GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder cb = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        cb.setCodeDigits(6).setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30)).setKeyRepresentation(KeyRepresentation.BASE32);
        googleAuthenticator = new GoogleAuthenticator(cb.build());
    }

    private String genSecretKey(String hash) {
        Base32 codec32 = new Base32();
        String hex = DigestUtils.md5Hex(hash).substring(0, 10);
        return codec32.encodeAsString(hex.getBytes());
    }

    public String genSecretKeyByUser(String email, String regDtm, String pwd) {
        return genSecretKey(email + "_" + regDtm + "_" + pwd);
    }

    public Boolean isOtpValid(User user, String otpCode) {
        if(!user.getOtpStatus().equals(OtpStatus.C.name())) {
            return false;
        } else {
            return googleAuthenticator.authorize(user.getOtpHash(), Integer.valueOf(otpCode));
        }
    }

    public Boolean isOtpValidForConfirm(User user, String otpCode) {
        return googleAuthenticator.authorize(user.getOtpHash(), Integer.valueOf(otpCode));
    }

    public String getQRBarcodeURL(User user, String otpHash) {
        String format = "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=otpauth://totp/bitwallet?secret=" + otpHash + "&issuer=bitwallet";
        return String.format(format, format);
    }
}
