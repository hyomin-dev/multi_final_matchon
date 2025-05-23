package com.multi.matchon.customerservice.controller;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.dto.res.FaqDto;
import com.multi.matchon.customerservice.service.FaqService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final MemberRepository memberRepository;

    // 카테고리 선택 후 키워드 검색 -> 조회
    @GetMapping("/cs")
    public String faqShow(@RequestParam(required = false) CustomerServiceType category,
                          @RequestParam(required = false) String keyword,
                          // DESC : 내림차순(최신 데이터가 먼저 추가됨..), ASC : 오름차순(최신 데이터를 뒤로 보냄..)
                          @PageableDefault(size = 8, sort = "createdDate", direction = Sort.Direction.ASC) Pageable pageable,
                          Model model) {

        Page<FaqDto> faqPage;

        if (keyword != null && !keyword.isBlank()) {
            if (category != null) {
                faqPage = faqService.searchByCategoryAndTitlePaged(category, keyword, pageable);
            } else {
                faqPage = faqService.searchByTitlePaged(keyword, pageable);
            }
        } else {
            faqPage = faqService.getFaqListPaged(category, pageable);
        }

        model.addAttribute("faqList", faqPage.getContent());
        model.addAttribute("page", faqPage);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        return "cs/cs";
    }

    // 상세보기
    @GetMapping("/faq/detail")
    public String faq_detail_Show() {
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

        Long savedId = faqService.savePost(faqDto.withMember(member));

        return "redirect:/cs";
    }

    // faq 상세보기 설정
    @GetMapping("/faq/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        FaqDto faqDto = faqService.getFaqById(id);
        model.addAttribute("faqDto", faqDto);
        return "cs/cs-faq-detail";
    }
}