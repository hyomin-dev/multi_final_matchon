package com.multi.matchon.community.repository;

import com.multi.matchon.community.domain.*;
import com.multi.matchon.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByReportTypeAndTargetIdAndReporter(ReportType type, Long targetId, Member reporter);

    Page<Report> findAll(Pageable pageable);

    int countByReportTypeAndTargetId(ReportType type, Long targetId);
}
