package com.multi.matchon.customerservice.controller;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.dto.res.FaqDto;
import com.multi.matchon.customerservice.service.FaqService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final MemberRepository memberRepository;

    // 카테고리 선택 후 키워드 검색 -> 조회
    @GetMapping("/cs")
    public String faqShow(@RequestParam(required = false) CustomerServiceType category,
                          @RequestParam(required = false) String keyword,
                          Model model) {

        List<FaqDto> faqList;

        if (keyword != null && !keyword.isBlank()) {
            if (category != null) {
                faqList = faqService.searchByCategoryAndTitle(category, keyword);
            } else {
                faqList = faqService.searchByTitle(keyword);
            }
        } else {
            faqList = faqService.getFaqList(category);
        }

        // 로그 출력
        System.out.println("선택된 카테고리: " + category);
        System.out.println("입력된 키워드: " + keyword);
        faqList.forEach(f -> System.out.println("조회된 제목: " + f.getFaqTitle()));

        model.addAttribute("faqList", faqList);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword); // 검색창 입력값 유지용
        return "cs/cs";
    }

    // 상세보기(챗봇 이용방법)
    @GetMapping("/faq/howtochatbot")
    public String faqChatbotPage() {
        return "cs/cs-faq-detail";
    }

    // faq 등록하기
    @GetMapping("/faq/register")
    public String write() {
        return "cs/cs-faq-register";
    }

    // 등록하기를 파라미터로 받는다
    @PostMapping("/faq/register")
    public String write(@RequestParam String faqTitle,
                        @RequestParam String faqContent,
                        @RequestParam CustomerServiceType faqCategory,
                        Principal principal) {

        String email = principal.getName();
        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        FaqDto faqDto = FaqDto.builder()
                .faqTitle(faqTitle)
                .faqContent(faqContent)
                .faqCategory(faqCategory)
                .build();

        faqService.savePost(faqDto.withMember(member));

        return "redirect:/cs";
    }

}
