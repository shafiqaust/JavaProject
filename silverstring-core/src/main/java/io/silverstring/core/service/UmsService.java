package io.silverstring.core.service;

import com.google.common.collect.Lists;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.domain.dto.UserSearchDTO;
import io.silverstring.domain.hibernate.EmailConfirm;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class UmsService {
    @Value("${spring.mail.username}")
    public String ADMIN_EMAIL;
    public String BASE_URL = System.getProperty("base.url");

    private final EmailService emailService;
    private final MessageSource messageSource;

    @Autowired
    public UmsService(EmailService emailService, MessageSource messageSource) {
        this.emailService = emailService;
        this.messageSource = messageSource;
    }

    @SoftTransational
    public void emailConfirmEmailSend(EmailConfirm emailConfirm) throws UnsupportedEncodingException, CannotSendEmailException {
        Locale userLocale = new Locale(emailConfirm.getLocale());

        log.debug(">>>> emailConfirmEmailSend locale : " + userLocale.getCountry() + "/" + userLocale.getLanguage());

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress(ADMIN_EMAIL, messageSource.getMessage("exchange.name", null, null, userLocale)))
                .to(Lists.newArrayList(new InternetAddress(emailConfirm.getEmail(), emailConfirm.getEmail())))
                .subject(messageSource.getMessage("email.confirm.subject", null, null, userLocale))
                .body("")
                .encoding("UTF-8").build();

        final Map<String, Object> modelObject = new HashMap<>();

        String confirmUrl = BASE_URL + "/emailConfirm" + "?hash=" + emailConfirm.getHashEmail() + "&code=" + emailConfirm.getCode();

        modelObject.put("confirm_url", confirmUrl);
        modelObject.put("exchange_name", messageSource.getMessage("exchange.name", null, null, userLocale));
        modelObject.put("email_confirm_subject", messageSource.getMessage("email.confirm.subject", null, null, userLocale));
        modelObject.put("email_confirm_desc1", messageSource.getMessage("email.confirm.desc1", null, null, userLocale));
        modelObject.put("email_confirm_btn",confirmUrl);
        modelObject.put("email_confirm_desc2", messageSource.getMessage("email.confirm.desc2", null, null, userLocale));

        emailService.send(email, "common/emailConfirmTemplate", modelObject);
    }

    @SoftTransational
    public void emailUserSearchSend(UserSearchDTO userSearch) throws UnsupportedEncodingException, CannotSendEmailException {
        Locale userLocale = new Locale(userSearch.getLocale());

        log.debug(">>>> emailUserSearchSend locale : " + userLocale.getCountry() + "/" + userLocale.getLanguage());

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress(ADMIN_EMAIL, messageSource.getMessage("exchange.name", null, null, userLocale)))
                .to(Lists.newArrayList(new InternetAddress(userSearch.getEmail(), userSearch.getEmail())))
                .subject(messageSource.getMessage("email.usersearch.subject", null, null, userLocale))
                .body("")
                .encoding("UTF-8").build();

        final Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("exchange_name", messageSource.getMessage("exchange.name", null, null, userLocale));
        modelObject.put("title", messageSource.getMessage("email.usersearch.title", null, null, userLocale));
        modelObject.put("content", messageSource.getMessage("email.usersearch.content", null, null, userLocale) + " " + userSearch.getCode());
        emailService.send(email, "common/emailUserSearch", modelObject);
    }

    public void emailSupport(String toEmail, String title, String content, String locale) throws UnsupportedEncodingException, CannotSendEmailException {
        Locale userLocale = new Locale(locale);

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress(ADMIN_EMAIL, messageSource.getMessage("exchange.name", null, null, userLocale)))
                .to(Lists.newArrayList(new InternetAddress(toEmail, toEmail)))
                .subject(title)
                .body("")
                .encoding("UTF-8").build();

        final Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("title", title);
        modelObject.put("content", content);
        modelObject.put("exchange_name", messageSource.getMessage("exchange.name", null, null, userLocale));
        modelObject.put("email_support_desc1", messageSource.getMessage("email.support.desc1", null, null, userLocale));
        emailService.send(email, "common/supportEmailTemplate", modelObject);
    }
}
