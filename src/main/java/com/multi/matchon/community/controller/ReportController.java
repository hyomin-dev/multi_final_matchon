package com.multi.matchon.community.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> reportContent(@RequestParam ReportType type,
                                           @RequestParam Long targetId,
                                           @RequestParam String reason,
                                           @AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            reportService.report(type, targetId, reason, user.getMember());
            return ResponseEntity.ok("신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
