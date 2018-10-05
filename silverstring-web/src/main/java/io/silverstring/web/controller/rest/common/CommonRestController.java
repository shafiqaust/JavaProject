package io.silverstring.web.controller.rest.common;

import io.silverstring.core.service.*;
import io.silverstring.domain.dto.*;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.hibernate.IcoRecommend;
import io.silverstring.domain.hibernate.Notice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonRestController {
    final NoticeService noticeService;
    final CoinService coinService;
    final IcoRecommendService icoRecommendService;
    final OtpService otpService;
    final UserService userService;
    final MessageSource messageSource;

    @Autowired
    public CommonRestController(NoticeService noticeService, CoinService coinService, IcoRecommendService icoRecommendService, UserService userService, OtpService otpService, MessageSource messageSource) {
        this.noticeService = noticeService;
        this.coinService = coinService;
        this.icoRecommendService = icoRecommendService;
        this.otpService = otpService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping("/getTopN1Notice")
    public Response<Notice> getTopN1Notice() {
        return Response.<Notice>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(noticeService.getTopN1Notice())
                .build();
    }

    @PostMapping("/getAllIcoRecommend")
    public Response<List<IcoRecommend>> getAllIcoRecommend() {
        return Response.<List<IcoRecommend>>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(icoRecommendService.getAll())
                .build();
    }

    @RequestMapping("/properties/{localeName}")
    public void getProperties(@PathVariable String localeName, HttpServletResponse response) throws Exception {
        String localeFileName = null;
        if("ko_KR".indexOf(localeName) >= 0) {
            localeFileName = "/messages/message_ko_KR.properties";
        } else {
            localeFileName = "/messages/message_en_US.properties";
        }

        OutputStream outputStream = response.getOutputStream();
        ClassPathResource resource = new ClassPathResource(localeFileName);
        InputStream inputStream = resource.getInputStream();
        List<String> readLines = IOUtils.readLines(inputStream);
        IOUtils.writeLines(readLines, null, outputStream);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
    }

    @GetMapping("/getLocale")
    public void getLocale(HttpServletResponse response) throws Exception {
        Locale _locale = LocaleContextHolder.getLocale();
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(_locale.getLanguage().getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @PostMapping("/getIsOtpUser")
    public Response<UserDTO.ResGetOtpStatus> getIsOtpUser(@RequestBody UserDTO.ReqGetOtpStatus request, HttpServletResponse response) throws Exception {
        return Response.<UserDTO.ResGetOtpStatus>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.getOtpStatus(null, request))
                .build();
    }
}
