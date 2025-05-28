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

}
