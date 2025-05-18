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


    @GetMapping("/signup/user")
    public String userSignup() {
        return "signup/user"; // 사용자 회원가입 화면
    }

    @GetMapping("/signup/host")
    public String hostSignup() {
        return "signup/host"; // 주최자 회원가입 화면
    }
}
