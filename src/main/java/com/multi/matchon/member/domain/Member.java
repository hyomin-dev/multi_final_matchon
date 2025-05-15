package com.multi.matchon.member.domain;

import com.multi.matchon.common.domain.BaseTimeEntity;
import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.TimeType;
import com.multi.matchon.team.domain.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sports_type_id", nullable = false)
    private SportsType sportsType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="position_id",nullable = false)
    private Positions positions;

    @Column(name="preferred_time", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TimeType timeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id",nullable = false)
    private Team team;

    @Column(name="my_temperature",nullable = false)
    @Builder.Default
    private Double myTemperature = 36.5;

    @Column(name="picture_attachment_enabled",columnDefinition = "BOOLEAN DEFAULT TRUE CHECK (picture_attachment_enabled=true)", nullable = false)
    private Boolean pictureAttachmentEnabled;

    @Column(name="is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted=false;





}
