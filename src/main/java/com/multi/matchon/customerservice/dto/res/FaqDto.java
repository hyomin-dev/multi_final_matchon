package com.multi.matchon.customerservice.dto.res;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import com.multi.matchon.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
public class FaqDto {

    private Long id;
    private Member member;
    private CustomerServiceType faqCategory;
    private String faqTitle;
    private String faqContent;
    private Boolean isDeleted;
    private LocalDateTime createdDate;

    @Builder
    public FaqDto(Long id,
                  Member member,
                  CustomerServiceType faqCategory,
                  String faqTitle,
                  String faqContent,
                  Boolean isDeleted,
                  LocalDateTime createdDate) {
        this.id = id;
        this.member = member;
        this.faqCategory = faqCategory;
        this.faqTitle = faqTitle;
        this.faqContent = faqContent;
        this.isDeleted = isDeleted;
        this.createdDate = createdDate;
    }


    public FaqDto withMember(Member member) {
        return FaqDto.builder()
                .id(this.id)
                .member(member)
                .faqCategory(this.faqCategory)
                .faqTitle(this.faqTitle)
                .faqContent(this.faqContent)
                .isDeleted(this.isDeleted)
                .createdDate(this.createdDate)
                .build();
    }


    public Faq toEntity() {
        return Faq.builder()
                .id(id)
                .member(member)
                .faqCategory(faqCategory)
                .faqTitle(faqTitle)
                .faqContent(faqContent)
                .isDeleted(isDeleted != null ? isDeleted : false)
                .build();
    }
}
