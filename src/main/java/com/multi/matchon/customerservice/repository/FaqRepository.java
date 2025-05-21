package com.multi.matchon.customerservice.repository;

import com.multi.matchon.customerservice.domain.CustomerServiceType;
import com.multi.matchon.customerservice.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByIsDeletedFalse();
    List<Faq> findByFaqCategoryAndIsDeletedFalse(CustomerServiceType category);
}
