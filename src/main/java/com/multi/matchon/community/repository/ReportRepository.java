package com.multi.matchon.community.repository;

import com.multi.matchon.community.domain.Report;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByReportTypeAndTargetIdAndReporter(ReportType type, Long targetId, Member reporter);

    int countByReportTypeAndTargetId(ReportType type, Long targetId);
}
