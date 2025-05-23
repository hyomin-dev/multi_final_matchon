package com.multi.matchon.customerservice.service;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import com.multi.matchon.customerservice.dto.res.FaqDto;
import com.multi.matchon.customerservice.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

//    public List<FaqDto> getFaqList(CustomerServiceType category, Pageable pageable) {
//        Page<Faq> page = (category != null)
//                ? faqRepository.findByFaqCategoryAndIsDeletedFalse(category, pageable)
//                : faqRepository.findByIsDeletedFalse(pageable);
//
//        List<Faq> faqs = page.getContent();
//
//        return faqs.stream()
//                .map(this::convertEntityToDto)
//                .collect(Collectors.toList());
//    }

//    public List<FaqDto> searchByTitle(String keyword, Pageable pageable) {
//        Page<Faq> faqs = faqRepository.findByFaqTitleContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
//        return faqs.stream()
//                .map(this::convertEntityToDto)
//                .collect(Collectors.toList());
//    }

//    public List<FaqDto> searchByCategoryAndTitle(CustomerServiceType category, String keyword, Pageable pageable) {
//        Page<Faq> faqs = faqRepository.findByFaqCategoryAndFaqTitleContainingIgnoreCaseAndIsDeletedFalse(category, keyword, pageable);
//        return faqs.stream()
//                .map(this::convertEntityToDto)
//                .collect(Collectors.toList());
//    }

    public Long savePost(FaqDto faqDto) {
        return faqRepository.save(faqDto.toEntity()).getId();
    }

    private FaqDto convertEntityToDto(Faq faq) {
        return FaqDto.builder()
                .faqId(faq.getId())
                .member(faq.getMember())
                .faqCategory(faq.getFaqCategory())
                .faqTitle(faq.getFaqTitle())
                .faqContent(faq.getFaqContent())
                .isDeleted(faq.getIsDeleted())
                .createdDate(faq.getCreatedDate())
                .build();
    }

    public FaqDto getFaqById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        return convertEntityToDto(faq);
    }

    public Page<FaqDto> searchByCategoryAndTitlePaged(CustomerServiceType category, String keyword, Pageable pageable) {
        return faqRepository.findByFaqCategoryAndFaqTitleContainingIgnoreCaseAndIsDeletedFalse(category, keyword, pageable)
                .map(this::convertEntityToDto);
    }

    public Page<FaqDto> searchByTitlePaged(String keyword, Pageable pageable) {
        return faqRepository.findByFaqTitleContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable)
                .map(this::convertEntityToDto);
    }

    public Page<FaqDto> getFaqListPaged(CustomerServiceType category, Pageable pageable) {
        Page<Faq> page = (category != null)
                ? faqRepository.findByFaqCategoryAndIsDeletedFalse(category, pageable)
                : faqRepository.findByIsDeletedFalse(pageable);

        List<FaqDto> distinctList = page.getContent().stream()
                .map(this::convertEntityToDto)
                .distinct()
                .collect(Collectors.toList());

        return new PageImpl<>(distinctList, pageable, page.getTotalElements());
    }
}
