package com.multi.matchon.customerservice.repository;

import com.multi.matchon.customerservice.domain.Inquiry;
import com.multi.matchon.customerservice.domain.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);
}
