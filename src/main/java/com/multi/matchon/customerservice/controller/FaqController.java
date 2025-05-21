package com.multi.matchon.customerservice.controller;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import com.multi.matchon.customerservice.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // CS 화면으로 이동. - faq 목록 조회
    @GetMapping("/cs")
    public String faqShow(@RequestParam(required = false)CustomerServiceType category, Model model) {
        List<Faq> faqList = faqService.getFaqList(category);

        // 로그
        System.out.println("선택된 카테고리: " + category);
        faqList.forEach(f -> System.out.println("조회된 카테고리: " + f.getFaqCategory()));

        model.addAttribute("faqList", faqList);
        model.addAttribute("category", category);
        return "cs/cs";
    }

    // 상세보기(챗봇 이용방법)
    @GetMapping("/faq/howtochatbot")
    public String faqChatbotPage() {
        return "cs/cs-faq-detail";
    }

    // 등록하기
    @GetMapping("/faq/register")
    public String write() {
        return "cs/cs-faq-register";
    }
}
