package com.multi.matchon.common.viewcontroller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/","/main"})
    public String mainPage(){
        return "main/main";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login/login";
    }

    @GetMapping("/signup")
    public String signupPage(){
        return "signup/signup";
    }

    @GetMapping("/header-footer-test")
    public String test(){
        return "common/header_footer_use";
    }
}
