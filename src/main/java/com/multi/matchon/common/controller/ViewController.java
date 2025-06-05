package com.multi.matchon.common.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    public String userSignup() {
        return "signup/signup";
    }

    @PostMapping("/error/chat")
    public ModelAndView showErrorPage(@RequestParam("error") String error, ModelAndView mv){
        mv.setViewName("common/error");
        mv.addObject("errorMessage", error);

        return mv;
    }

    @GetMapping("/error/authorities")
    public ModelAndView showErrorPageByAuthorities(ModelAndView mv){
        mv.setViewName("common/error");
        mv.addObject("errorMessage", "403 접근할 권한이 없습니다.");

        return mv;
    }

//    @GetMapping("/error/authentication")
//    public ModelAndView showErrorPageByAuthentication(ModelAndView mv){
//        mv.setViewName("common/error");
//        mv.addObject("errorMessage", "401 로그인이 필요한 서비스입니다.");
//
//        return mv;
//    }





}
