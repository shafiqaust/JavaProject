package io.silverstring.web.controller.view.landing;

import io.silverstring.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${security.require-ssl}")
    private boolean SECURITY_REQUEST_SSL;

    final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/doRegist", method= RequestMethod.POST)
    public String doSignup(@RequestParam("email") String email, @RequestParam("pwd") String pwd, @RequestParam("repwd") String repwd) {
        try {
            log.info("doRegist : {}", email);
            return userService.doRegist(email, pwd, repwd);
        } catch (Exception ex) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/regist?msg=invalid" : "/regist?msg=invalid");
        }
    }

    @RequestMapping(value = "/doUserSearch", method= RequestMethod.POST)
    public String doUserSearch(@RequestParam("email") String email) {
        try {
            log.info("doUserSearch : {}", email);
            return userService.doUserSearch(email);
        } catch (Exception ex) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/userSearch?msg=invalid" : "/userSearch?msg=invalid");
        }
    }

    @RequestMapping(value = "/doEmailSearch", method= RequestMethod.POST)
    public String doEmailSearch(@RequestParam("email") String email) {
        try {
            log.info("doEmailSearch : {}", email);
            return userService.doEmailSearch(email);
        } catch (Exception ex) {
            return "redirect:" + (SECURITY_REQUEST_SSL == true ? System.getProperty("base.url") + "/emailSearch?msg=invalid" : "/emailSearch?msg=invalid");
        }
    }
}
