package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.ReasonType;
import com.multi.matchon.community.domain.Report;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.dto.res.ReportResponse;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.community.repository.CommentRepository;
import com.multi.matchon.community.repository.ReportRepository;
import com.multi.matchon.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public void report(ReportType type, Long targetId, String reason, ReasonType reasonType, Member reporter) {
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
                .reasonType(reasonType)
                .reporter(reporter)
                .build();

        reportRepository.save(report);
    }

    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));

        return reports.stream().map(report -> {
            String targetWriterName = null;

            if (report.getReportType() == ReportType.BOARD) {
                targetWriterName = boardRepository.findById(report.getTargetId())
                        .map(board -> board.getMember().getMemberName())
                        .orElse(null);
            } else if (report.getReportType() == ReportType.COMMENT) {
                targetWriterName = commentRepository.findById(report.getTargetId())
                        .map(comment -> comment.getMember().getMemberName())
                        .orElse(null);
            }

            return ReportResponse.builder()
                    .id(report.getId())
                    .reportType(report.getReportType().name())
                    .targetId(report.getTargetId())
                    .targetWriterName(targetWriterName)
                    .reporterName(report.getReporter().getMemberName())
                    .reasonType(report.getReasonType().getLabel())
                    .reason(report.getReason())
                    .createdDate(report.getCreatedDate())
                    .boardId(resolveBoardId(report))
                    .build();
        }).collect(Collectors.toList());
    }

    private Long resolveBoardId(Report report) {
        if (report.getReportType() == ReportType.BOARD) {
            return report.getTargetId(); // 게시글 ID 자체
        } else if (report.getReportType() == ReportType.COMMENT) {
            return commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getBoard().getId())
                    .orElse(null); // 댓글이 속한 게시글 ID
        }
        return null;
    }

}
