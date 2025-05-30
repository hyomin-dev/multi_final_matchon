package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.community.domain.ReasonType;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.dto.res.ReportResponse;
import com.multi.matchon.community.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/community/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 신고 접수
    @PostMapping
    public ResponseEntity<?> reportContent(@RequestParam ReportType type,
                                           @RequestParam Long targetId,
                                           @RequestParam String reason,
                                           @RequestParam ReasonType reasonType,
                                           @AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            reportService.report(type, targetId, reason, reasonType, user.getMember());
            return ResponseEntity.ok("신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 전체 신고 목록 조회 (관리자용)
    @GetMapping("/all")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    // 3. 페이징된 신고 목록 조회
    @GetMapping("/page")
    public ResponseEntity<Page<ReportResponse>> getPagedReports(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        Page<ReportResponse> pagedReports = reportService.getReportsPaged(page, size);
        return ResponseEntity.ok(pagedReports);
    }

    private void addPagingAttributes(Model model, int page, int size) {
        Page<ReportResponse> pagedReports = reportService.getReportsPaged(page, size);
        model.addAttribute("reports", pagedReports.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedReports.getTotalPages());
    }


    @GetMapping("/reports")
    public String reportPage(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             Model model) {
        addPagingAttributes(model, page, size);
        return "admin/report";
    }

    @GetMapping("/reports/page")
    public String getReportsPaged(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model) {
        addPagingAttributes(model, page, size);
        return "admin/report :: reportTableArea";
    }


}
