package com.multi.matchon.customerservice.repository;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    Page<Faq> findByIsDeletedFalse(Pageable pageable);
    Page<Faq> findByFaqCategoryAndIsDeletedFalse(CustomerServiceType category, Pageable pageable);
    Page<Faq> findByFaqTitleContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);
    Page<Faq> findByFaqCategoryAndFaqTitleContainingIgnoreCaseAndIsDeletedFalse(CustomerServiceType category, String keyword, Pageable pageable);
}
