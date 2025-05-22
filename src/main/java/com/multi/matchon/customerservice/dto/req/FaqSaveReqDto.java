package com.multi.matchon.customerservice.dto.req;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 요청.
@Getter
@NoArgsConstructor
public class FaqSaveReqDto {
    private Long memberId;
    private CustomerServiceType faqCategory;
    private String faqTitle;
    private String faqContent;

    @Builder
    public FaqSaveReqDto(Long memberId, CustomerServiceType faqCategory, String faqTitle, String faqContent) {
        this.memberId = memberId;
        this.faqCategory = faqCategory;
        this.faqTitle = faqTitle;
        this.faqContent = faqContent;
    }
}
