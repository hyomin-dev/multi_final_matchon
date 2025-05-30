package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.community.domain.ReasonType;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.dto.res.ReportResponse;
import com.multi.matchon.community.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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

    // 2. 신고 목록 조회 (관리자용)
    @GetMapping("/all")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }
}
