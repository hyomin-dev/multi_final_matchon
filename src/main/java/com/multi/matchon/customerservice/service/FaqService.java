package com.multi.matchon.customerservice.service;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import com.multi.matchon.customerservice.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<Faq> getFaqList(CustomerServiceType category) {
        return (category != null)
                ? faqRepository.findByFaqCategoryAndIsDeletedFalse(category)
                : faqRepository.findByIsDeletedFalse(); // 전체 목록
    }
}
