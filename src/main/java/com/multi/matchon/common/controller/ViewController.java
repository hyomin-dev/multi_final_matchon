package com.multi.matchon.common.controller;


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
}
