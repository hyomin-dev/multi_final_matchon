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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /**
     * 신고 접수 처리
     */
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

    /**
     * 전체 신고 목록 조회 (비페이징)
     */
    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        return reports.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * 페이징된 신고 목록 조회
     */
    public Page<ReportResponse> getReportsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return reportRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Report → ReportResponse 변환
     */
    private ReportResponse convertToResponse(Report report) {
        String targetWriterName = resolveTargetWriterName(report);
        Long boardId = resolveBoardId(report);

        Member targetMember = null;

        if (report.getReportType() == ReportType.BOARD) {
            targetMember = boardRepository.findById(report.getTargetId())
                    .map(board -> board.getMember())
                    .orElse(null);
        } else if (report.getReportType() == ReportType.COMMENT) {
            targetMember = commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getMember())
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
                .boardId(boardId)
                .targetMemberId(targetMember != null ? targetMember.getId() : null)
                .suspended(targetMember != null && targetMember.isSuspended())
                .targetIsAdmin(targetMember != null && targetMember.getMemberRole().name().equals("ADMIN"))
                .build();
    }


    /**
     * 신고 대상 작성자 이름 조회
     */
    private String resolveTargetWriterName(Report report) {
        if (report.getReportType() == ReportType.BOARD) {
            return boardRepository.findById(report.getTargetId())
                    .map(board -> board.getMember().getMemberName())
                    .orElse(null);
        } else if (report.getReportType() == ReportType.COMMENT) {
            return commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getMember().getMemberName())
                    .orElse(null);
        }
        return null;
    }


    /**
     * 댓글의 경우 해당 댓글이 포함된 게시글 ID 조회
     */

    private Long resolveBoardId(Report report) {
        if (report.getReportType() == ReportType.BOARD) {
            return report.getTargetId();
        } else if (report.getReportType() == ReportType.COMMENT) {
            return commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getBoard().getId())
                    .orElse(null);
        }
        return null;
    }
}
