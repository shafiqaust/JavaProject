package io.silverstring.core.service;

import io.silverstring.core.annotation.HardTransational;
import io.silverstring.core.annotation.SoftTransational;
import io.silverstring.core.exception.ExchangeException;
import io.silverstring.core.provider.MqPublisher;
import io.silverstring.core.repository.hibernate.*;
import io.silverstring.core.util.KeyGenUtil;
import io.silverstring.domain.dto.UserDTO;
import io.silverstring.domain.dto.UserSearchDTO;
import io.silverstring.domain.dto.WalletDTO;
import io.silverstring.domain.enums.*;
import io.silverstring.domain.hibernate.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final CoinRepository coinRepository;
    private final EmailConfirmRepository emailConfirmRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MqPublisher mqPublisher;
    private final OtpService otpService;
    private final UmsService umsService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Autowired
    public UserService(UserRepository userRepository, WalletService walletService, CoinRepository coinRepository, EmailConfirmRepository emailConfirmRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MqPublisher mqPublisher, OtpService otpService, UmsService umsService, ModelMapper modelMapper, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.coinRepository = coinRepository;
        this.emailConfirmRepository = emailConfirmRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mqPublisher = mqPublisher;
        this.otpService = otpService;
        this.umsService = umsService;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
    }

    @SoftTransational
    public UserDTO.ResReleaseMember releaseMember(User user, UserDTO.ReqReleaseMember request) {
        User existUser = userRepository.findOne(user.getId());
        if ((existUser == null) || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        if((request.getOtp() == null) || request.getOtp().equals("")) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        } else if(!otpService.isOtpValid(existUser,  request.getOtp())) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        WalletDTO.WalletInfos walletInfos = walletService.getMyWallets(existUser.getId(), user.getLevel(), true);
        for (WalletDTO.WalletInfos.Info info : walletInfos.getInfos()) {
            if ((info.getWallet().getAvailableBalance().floatValue() * 100000000) > 0) {
                throw new ExchangeException(CodeEnum.WALLET_IS_NOT_ZERO);
            }
        }

        existUser.setActive(ActiveEnum.N);
        existUser.setDelDtm(LocalDateTime.now());
        userRepository.save(existUser);

        return new UserDTO.ResReleaseMember();
    }

    @SoftTransational
    public UserDTO.ResChangePassword changePassword(User user, UserDTO.ReqChangePassword request) {
        if (!request.getNewPassword().equals(request.getNewPasswordRe())) {
            throw new ExchangeException(CodeEnum.INPUT_VALUE_INCORRECT);
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPwd())) {
            throw new ExchangeException(CodeEnum.INPUT_VALUE_INCORRECT);
        }

        User existUser = userRepository.findOne(user.getId());
        if((existUser == null)  || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.INPUT_VALUE_INCORRECT);
        }

        if((request.getOtp() == null) || request.getOtp().equals("")) {
            throw new ExchangeException(CodeEnum.INPUT_VALUE_INCORRECT);
        } else if(!otpService.isOtpValid(existUser,  request.getOtp())) {
            throw new ExchangeException(CodeEnum.INPUT_VALUE_INCORRECT);
        }

        existUser.setPwd(bCryptPasswordEncoder.encode(request.getNewPassword()));

        return modelMapper.map(existUser, UserDTO.ResChangePassword.class);
    }

    public Optional<User> getActiveUserByEmailAndPwd(String email, String pwd) {
        return Optional.ofNullable(userRepository.findOneByEmailAndPwdAndDelDtmIsNullAndActive(email, pwd, ActiveEnum.Y));
    }

    public Optional<User> getActiveUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findOneByEmailAndDelDtmIsNullAndActive(email, ActiveEnum.Y));
    }

    @SoftTransational
    public UserDTO.ResEmailConfirm emailConfirm(String hashEmaul, String code) {
        EmailConfirmPK emailConfirmPK = new EmailConfirmPK();
        emailConfirmPK.setHashEmail(hashEmaul);
        emailConfirmPK.setCode(code);

        EmailConfirm emailConfirm = emailConfirmRepository.findOne(emailConfirmPK);
        if (emailConfirm == null) {
            UserDTO.ResEmailConfirm resEmailConfirm = new UserDTO.ResEmailConfirm();
            Locale locale = LocaleContextHolder.getLocale();
            resEmailConfirm.setTitle(messageSource.getMessage("email.confirm1.title", null, null, locale));
            resEmailConfirm.setMsg(messageSource.getMessage("email.confirm1.msg", null, null, locale));
            resEmailConfirm.setUrl("/regist");
            resEmailConfirm.setUrlTitle(messageSource.getMessage("email.confirm1.url.title", null, null, locale));
            return resEmailConfirm;
        }

        Locale locale = new Locale(emailConfirm.getLocale());

        if (ActiveEnum.Y.equals(emailConfirm.getSendYn())) {
            UserDTO.ResEmailConfirm resEmailConfirm = new UserDTO.ResEmailConfirm();
            resEmailConfirm.setTitle(messageSource.getMessage("email.confirm2.title", null, null, locale));
            resEmailConfirm.setMsg(messageSource.getMessage("email.confirm2.msg", null, null, locale));
            resEmailConfirm.setUrl("/regist");
            resEmailConfirm.setUrlTitle(messageSource.getMessage("email.confirm2.url.title", null, null, locale));
            return resEmailConfirm;
        }
        emailConfirm.setSendYn(ActiveEnum.Y);

        User existUser = userRepository.findOneByEmail(emailConfirm.getEmail());
        if (existUser == null) {
            UserDTO.ResEmailConfirm resEmailConfirm = new UserDTO.ResEmailConfirm();
            resEmailConfirm.setTitle(messageSource.getMessage("email.confirm3.title", null, null, locale));
            resEmailConfirm.setMsg(messageSource.getMessage("email.confirm3.msg", null, null, locale));
            resEmailConfirm.setUrl("/regist");
            resEmailConfirm.setUrlTitle(messageSource.getMessage("email.confirm3.url.title", null, null, locale));
            return resEmailConfirm;
        }

        if (ActiveEnum.Y.equals(existUser.getActive())) {
            UserDTO.ResEmailConfirm resEmailConfirm = new UserDTO.ResEmailConfirm();
            resEmailConfirm.setTitle(messageSource.getMessage("email.confirm4.title", null, null, locale));
            resEmailConfirm.setMsg(messageSource.getMessage("email.confirm4.msg", null, null, locale));
            resEmailConfirm.setUrl("/login");
            resEmailConfirm.setUrlTitle(messageSource.getMessage("email.confirm4.url.title", null, null, locale));
            return resEmailConfirm;
        }

        existUser.setActive(ActiveEnum.Y);

        UserDTO.ResEmailConfirm resEmailConfirm = new UserDTO.ResEmailConfirm();
        resEmailConfirm.setTitle(messageSource.getMessage("email.confirm5.title", null, null, locale));
        resEmailConfirm.setMsg(messageSource.getMessage("email.confirm5.msg", null, null, locale));
        resEmailConfirm.setUrl("/login");
        resEmailConfirm.setUrlTitle(messageSource.getMessage("email.confirm5.url.title", null, null, locale));

        List<Coin> coins = coinRepository.findAll();
        for (Coin coin : coins) {
            if(coin.getName().equals(CoinEnum.BITCOIN))  //removing BTC for now
            {
                continue;
            }
            walletService.precreateWallet(existUser.getId(), coin.getName());
        }

        return resEmailConfirm;
    }

    @HardTransational
    public String doRegist(String email, String pwd, String repwd) {
        Locale locale = LocaleContextHolder.getLocale();

        User existUser = userRepository.findOneByEmail(email);
        if (existUser != null) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=already" : "/regist?msg=already");
        }

        if ((email == null) || ((email != null) && (email.indexOf("@") < 0))) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=invalid" : "/regist?msg=invalid");
        }

        if ((pwd == null) || ((pwd != null) && (pwd.length() < 8))) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=invalid" : "/regist?msg=invalid");
        }

        if (!pwd.equals(repwd)) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=invalid" : "/regist?msg=invalid");
        }

        User user = new User();
        user.setEmail(email);
        user.setPwd(bCryptPasswordEncoder.encode(pwd));
        user.setActive(ActiveEnum.N);
        user.setOtpStatus(ActiveEnum.N.name());
        user.setDelDtm(null);
        user.setRegDtm(LocalDateTime.now());
        user.setLevel(LevelEnum.LEVEL1);
        userRepository.save(user);

        String code = KeyGenUtil.generateEmailConfirmNumericKey();
        String hashEmail = KeyGenUtil.generateHashEmail(user.getId(), user.getPwd(), user.getEmail());
        EmailConfirm existEmailConfirm = emailConfirmRepository.findOneByEmail(user.getEmail());
        EmailConfirm emailConfirm = new EmailConfirm();
        if (existEmailConfirm != null) {
            existEmailConfirm.setCode(code);
            existEmailConfirm.setRegDtm(LocalDateTime.now());
            existEmailConfirm.setSendYn(ActiveEnum.N);
            existEmailConfirm.setLocale(locale.getLanguage() + "_" + locale.getCountry().replace("_", ""));
            emailConfirm = existEmailConfirm;
        } else {
            emailConfirm.setHashEmail(hashEmail);
            emailConfirm.setCode(code);
            emailConfirm.setEmail(email);
            emailConfirm.setRegDtm(LocalDateTime.now());
            emailConfirm.setSendYn(ActiveEnum.N);
            emailConfirm.setLocale(locale.getLanguage() + "_" + locale.getCountry().replace("_", ""));
            emailConfirmRepository.save(emailConfirm);
        }

        try {
            EmailConfirm bindEmailConfirm = new EmailConfirm();
            bindEmailConfirm.setHashEmail(emailConfirm.getHashEmail());
            bindEmailConfirm.setCode(emailConfirm.getCode());
            bindEmailConfirm.setEmail(emailConfirm.getEmail());
            bindEmailConfirm.setLocale(emailConfirm.getLocale());
            umsService.emailConfirmEmailSend(bindEmailConfirm);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=invalid" : "/regist?msg=invalid");
        }

        return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=succeed" : "/regist?msg=succeed");
    }

    @SoftTransational
    public String doUserSearch(String email) {
        Locale locale = LocaleContextHolder.getLocale();

        User existUser = userRepository.findOneByEmail(email);
        if (existUser == null) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/userSearch?msg=invalid" : "/userSearch?msg=invalid");
        }

        String code = KeyGenUtil.generateEmailConfirmNumericKey();
        existUser.setPwd(bCryptPasswordEncoder.encode(code));

        UserSearchDTO userSearch = new UserSearchDTO();
        userSearch.setEmail(email);
        userSearch.setCode(code);
        userSearch.setLocale(locale.getLanguage() + "_" + locale.getCountry().replace("_", ""));

        try {
            umsService.emailUserSearchSend (userSearch);
        } catch (Exception ex) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=invalid" : "/emailSearch?msg=invalid");
        }

        return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/userSearch?msg=succeed" : "/userSearch?msg=succeed");
    }

    @SoftTransational
    public String doEmailSearch(String email) {
        User existUser = userRepository.findOneByEmail(email);
        if (existUser == null) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=invalid" : "/emailSearch?msg=invalid");
        }

        EmailConfirm existEmailConfirm = emailConfirmRepository.findOneByEmail(email);
        if (existEmailConfirm == null) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=invalid" : "/emailSearch?msg=invalid");
        }

        existEmailConfirm.setRegDtm(LocalDateTime.now());
        existEmailConfirm.setSendYn(ActiveEnum.N);

        try {
            umsService.emailConfirmEmailSend(existEmailConfirm);
        } catch (Exception ex) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=invalid" : "/emailSearch?msg=invalid");
        }

        return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=succeed" : "/emailSearch?msg=succeed");
    }

    @SoftTransational
    public UserDTO.ResChangeOtpKey releaseOtp(User user, UserDTO.ReqChangeOtpKey request) {
        User existUser = userRepository.findOne(user.getId());
        if((existUser == null)  || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        if(request.getOtpReleaseValue() == null || request.getOtpReleaseValue().equals("")) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        if(!otpService.isOtpValid(existUser,  request.getOtpReleaseValue())) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        existUser.setOtpHash("");
        existUser.setOtpStatus(OtpStatus.N.name());

        return UserDTO.ResChangeOtpKey.builder().build();
    }

    @SoftTransational
    public UserDTO.ResChangeOtpKey confirmOtp(User user, UserDTO.ReqChangeOtpKey request) {
        User existUser = userRepository.findOne(user.getId());
        if((existUser == null)  || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        if(request.getOtpConfirmValue() == null || request.getOtpConfirmValue().equals("")) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        if(!otpService.isOtpValidForConfirm(existUser,  request.getOtpConfirmValue())) {
            throw new ExchangeException(CodeEnum.INVALID_OTP);
        }

        existUser.setOtpStatus(OtpStatus.C.name());

        return UserDTO.ResChangeOtpKey.builder().build();
    }

    @SoftTransational
    public UserDTO.ResChangeOtpKey changeOtpKey(User user, UserDTO.ReqChangeOtpKey request) {
         User existUser = userRepository.findOne(user.getId());
        if((existUser == null)  || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        if(existUser.getOtpStatus().equals(OtpStatus.C.name())) {
            throw new ExchangeException(CodeEnum.ALREADY_OTP_CREATE);
        }

        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        String nowDtime = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));

        String otpHash = otpService.genSecretKeyByUser(existUser.getEmail(), nowDtime, existUser.getPwd());
        String qrbarcodeURL = otpService.getQRBarcodeURL(existUser, otpHash);
        existUser.setOtpHash(otpHash);
        existUser.setOtpStatus(OtpStatus.P.name());

        return UserDTO.ResChangeOtpKey.builder().qrbarcodeURL(URLEncoder.encode(qrbarcodeURL)).otpCode(otpHash).build();
    }

    @SoftTransational
    public UserDTO.ResGetOtpStatus getOtpStatus(User user, UserDTO.ReqGetOtpStatus request) {
        User existUser = null;
        if(user != null) {
            existUser = userRepository.findOne(user.getId());
        } else {
            existUser = userRepository.findOneByEmail(request.getEmail());
        }

        if((existUser == null)  || (existUser != null && existUser.getDelDtm() != null)) {
            throw new ExchangeException(CodeEnum.USER_NOT_EXIST);
        }

        if((existUser.getOtpHash() == null) || existUser.getOtpHash().equals("")) {
            return UserDTO.ResGetOtpStatus.builder().otpStatus(OtpStatus.N.name()).build();
        } else {
            return UserDTO.ResGetOtpStatus.builder().otpStatus(existUser.getOtpStatus()).build();
        }
    }
}
