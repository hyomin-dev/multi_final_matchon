package com.multi.matchon.member.domain;

import com.multi.matchon.common.domain.BaseTimeEntity;
import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.TimeType;
import com.multi.matchon.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
//@Setter: JPA entity에서 setter사용은 자제, test용
@Table(name="member", uniqueConstraints = {@UniqueConstraint(name="UK_member_email",columnNames = {"member_email"})

})

public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @Column(name="member_email",nullable = false, columnDefinition = "VARCHAR(100)")
    private String memberEmail;

    @Column(name="member_password",nullable = false, columnDefinition = "VARCHAR(100)")
    private String memberPassword;

    @Column(name="member_name", nullable = false, columnDefinition = "VARCHAR(50)")
    private String memberName;

    @Column(name="member_role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberRole memberRole;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="position_id")
    private Positions positions;

    @Setter
    @Column(name="preferred_time")
    @Enumerated(value = EnumType.STRING)
    private TimeType timeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;

    @Setter
    @Column(name="my_temperature")
    private Double myTemperature; // nullable: 사용자만 사용

    @Column(name = "picture_attachment_enabled")
    private Boolean pictureAttachmentEnabled; // nullable : 관리자는 null 가능, 사용자는 회원가입 시 true로 설정

    @Column(name="is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted=false;

    // 임시비밀번호용
    @Column(name = "is_temporary_password", nullable = false)
    @Builder.Default
    private Boolean isTemporaryPassword = false;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;  // 정지 기한. null이면 정지 아님.

    public boolean isSuspended() {
        return suspendedUntil != null && LocalDateTime.now().isBefore(suspendedUntil);
    }

    public void suspend(int days) {
        this.suspendedUntil = LocalDateTime.now().plusDays(days);
    }

    public void suspendPermanently() {
        this.suspendedUntil = LocalDateTime.of(9999, 12, 31, 23, 59);
    }

    public void unsuspend() {
        this.suspendedUntil = null;
    }



    // 삭제
    public void markAsDeleted() {
        this.isDeleted = true;
    }

    // 복원
    public void unmarkAsDeleted() {
        this.isDeleted = false;
    }

    public void restoreAsUser(String encodedPassword, String name) {
        this.unmarkAsDeleted();
        this.memberPassword = encodedPassword;
        this.memberName = name;
        this.memberRole = MemberRole.USER;
        this.pictureAttachmentEnabled = true;
        this.myTemperature = 36.5;

        this.positions = null;
        this.timeType = null;
    }

    public void restoreAsHost(String encodedPassword, String name) {
        this.unmarkAsDeleted();
        this.memberPassword = encodedPassword;
        this.memberName = name;
        this.memberRole = MemberRole.HOST;
        this.pictureAttachmentEnabled = true;
    }

    public void clearPersonalInfo() {
        this.positions = null;
        this.timeType = null;
        this.myTemperature = null;
        this.pictureAttachmentEnabled = null;
    }

    public void updatePassword(String encodedPassword) {
        this.memberPassword = encodedPassword;
    }

    public void setIsTemporaryPassword(boolean isTemporaryPassword) {
        this.isTemporaryPassword = isTemporaryPassword;
    }

}
