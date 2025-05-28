package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Report;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.repository.ReportRepository;
import com.multi.matchon.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void report(ReportType type, Long targetId, String reason, Member reporter) {
        boolean alreadyReported = reportRepository
                .findByReportTypeAndTargetIdAndReporter(type, targetId, reporter)
                .isPresent();

        if (alreadyReported) {
            throw new IllegalStateException("이미 신고한 대상입니다.");
        }

        Report report = Report.builder()
                .reportType(type)
                .targetId(targetId)
                .reason(reason)
                .reporter(reporter)
                .build();

        reportRepository.save(report);
    }
}
