package com.multi.matchon.member.domain;

import com.multi.matchon.common.domain.BaseTimeEntity;
import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.TimeType;
import com.multi.matchon.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId()) && Objects.equals(getMemberEmail(), member.getMemberEmail()) && Objects.equals(getMemberPassword(), member.getMemberPassword()) && Objects.equals(getMemberName(), member.getMemberName()) && getMemberRole() == member.getMemberRole() && Objects.equals(getPositions(), member.getPositions()) && getTimeType() == member.getTimeType() && Objects.equals(getTeam(), member.getTeam()) && Objects.equals(getMyTemperature(), member.getMyTemperature()) && Objects.equals(getPictureAttachmentEnabled(), member.getPictureAttachmentEnabled()) && Objects.equals(getIsDeleted(), member.getIsDeleted());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMemberEmail(), getMemberPassword(), getMemberName(), getMemberRole(), getPositions(), getTimeType(), getTeam(), getMyTemperature(), getPictureAttachmentEnabled(), getIsDeleted());
    }

}
