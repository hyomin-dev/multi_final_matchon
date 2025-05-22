package com.multi.matchon.customerservice.service;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import com.multi.matchon.customerservice.dto.res.FaqDto;
import com.multi.matchon.customerservice.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqDto> getFaqList(CustomerServiceType category) {
        List<Faq> faqs = (category != null)
                ? faqRepository.findByFaqCategoryAndIsDeletedFalse(category)
                : faqRepository.findByIsDeletedFalse();

        return faqs.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    public List<FaqDto> searchByTitle(String keyword) {
        List<Faq> faqs = faqRepository.findByFaqTitleContainingIgnoreCaseAndIsDeletedFalse(keyword);
        return faqs.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    public List<FaqDto> searchByCategoryAndTitle(CustomerServiceType category, String keyword) {
        List<Faq> faqs = faqRepository.findByFaqCategoryAndFaqTitleContainingIgnoreCaseAndIsDeletedFalse(category, keyword);
        return faqs.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    public Long savePost(FaqDto faqDto) {
        return faqRepository.save(faqDto.toEntity()).getId();
    }

    private FaqDto convertEntityToDto(Faq faq) {
        return FaqDto.builder()
                .id(faq.getId())
                .member(faq.getMember())
                .faqCategory(faq.getFaqCategory())
                .faqTitle(faq.getFaqTitle())
                .faqContent(faq.getFaqContent())
                .isDeleted(faq.getIsDeleted())
                .createdDate(faq.getCreatedDate())
                .build();
    }
}
