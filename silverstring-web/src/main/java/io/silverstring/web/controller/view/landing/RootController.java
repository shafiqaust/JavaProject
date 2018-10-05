package io.silverstring.web.controller.view.landing;

import io.silverstring.core.service.CoinService;
import io.silverstring.core.service.UserService;
import io.silverstring.domain.dto.CoinDTO;
import io.silverstring.domain.dto.UserDTO;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class RootController {
    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    final UserService userService;
    final CoinService coinService;

    @Autowired
    public RootController(UserService userService, CoinService coinService) {
        this.userService = userService;
        this.coinService = coinService;
    }

    @RequestMapping("/")
    public ModelAndView root(HttpServletRequest request) {
        ModelAndView mvn = new ModelAndView("landing/index");
        return mvn;
    }

    @RequestMapping("/login")
    public ModelAndView login(@RequestParam(name = "msg", defaultValue = "none") String msg) {
        ModelAndView mvn = new ModelAndView("landing/login");
        mvn.addObject("msg", msg);
        mvn.addObject("baseUrl", System.getProperty("base.url"));
        return mvn;
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();

        if(user != null) {
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            log.info(">>>> " + user.getEmail() + " logout!!!.......");
        }

        return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/" : "/");
    }

    @RequestMapping("/term")
    public ModelAndView term(@RequestParam(name = "msg", defaultValue = "none") String msg) {
        ModelAndView mvn = new ModelAndView("landing/term");
        return mvn;
    }

    @RequestMapping("/regist")
    public ModelAndView regist(@RequestParam(name = "msg", defaultValue = "none") String msg) {
        ModelAndView mvn = new ModelAndView("landing/regist");
        mvn.addObject("msg", msg);
        return mvn;
    }

    @RequestMapping("/userSearch")
    public ModelAndView userSearch(@RequestParam(name = "msg", defaultValue = "none") String msg) {
        ModelAndView mvn = new ModelAndView("landing/userSearch");
        mvn.addObject("msg", msg);
        return mvn;
    }

    @RequestMapping("/emailSearch")
    public ModelAndView emailSearch(@RequestParam(name = "msg", defaultValue = "none") String msg) {
        ModelAndView mvn = new ModelAndView("landing/emailSearch");
        mvn.addObject("msg", msg);
        return mvn;
    }

    @RequestMapping("/emailConfirm")
    public ModelAndView emailConfirm(@RequestParam("hash") String hash, @RequestParam("code") String code) {
        ModelAndView mvn = new ModelAndView("common/emailConfirm");
        UserDTO.ResEmailConfirm resEmailConfirm = userService.emailConfirm(hash, code);
        mvn.addObject("title", resEmailConfirm.getTitle());
        mvn.addObject("msg", resEmailConfirm.getMsg());
        mvn.addObject("url", resEmailConfirm.getUrl());
        mvn.addObject("urlTitle", resEmailConfirm.getUrlTitle());
        return mvn;
    }
}
