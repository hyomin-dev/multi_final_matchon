package com.multi.matchon.community.domain;

import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reportType", "targetId", "reporter_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글인지 댓글인지 구분
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    // 대상 ID (게시글 ID 혹은 댓글 ID)
    @Column(nullable = false)
    private Long targetId;

    // 신고자 (사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    // 신고 사유 (간단한 텍스트)
    @Column(nullable = false, length = 1000)
    private String reason;

    // 신고 일시
    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @PrePersist
    protected void onCreate() {
        this.reportedAt = LocalDateTime.now();
    }
}
