package com.multi.matchon.common.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

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

    @GetMapping("/redirect")
    public String redirectHandler(@RequestParam("url") String url, HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);
            // 로그인 안된 경우 → login.html에서 JS로 이동 처리
            model.addAttribute("redirectUrl", url);
            return "login/login";
        }

        return "redirect:" + url; // 로그인된 경우에만 redirect
    }

//    @GetMapping("/error/authentication")
//    public ModelAndView showErrorPageByAuthentication(ModelAndView mv){
//        mv.setViewName("common/error");
//        mv.addObject("errorMessage", "401 로그인이 필요한 서비스입니다.");
//
//        return mv;
//    }





}
