package com.multi.matchon.guest.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.TimeType;
import com.multi.matchon.member.domain.Member;
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
@Table(name="guest_board")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class GuestBoard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="guest_board_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id",nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sports_type_id",nullable = false)
    private SportsType sportsType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="preferred_position_id",nullable = false)
    private Positions positions;

    @Column(name="picture_attachment_enabled",nullable = false,columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE CHECK (picture_attachment_enabled=true)")
    private Boolean pictureAttachmentEnabled;

    @Column(name="self_intro",nullable = false,columnDefinition = "TEXT")
    private String selfIntro;

    @Column(name="preferred_region1",nullable = false,columnDefinition = "VARCHAR(50)")
    private String preferredRegion1;

    @Column(name="preferred_region2",columnDefinition = "VARCHAR(50)")
    private String preferredRegion2;

    @Column(name="preferred_region3",columnDefinition = "VARCHAR(50)")
    private String preferredRegion3;

    @Column(name="preferred_time1",nullable = false,columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NOT NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType1 = TimeType.WEEKEND_MORNING;

    @Column(name="preferred_time2",columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType2 = TimeType.WEEKEND_MORNING;

    @Column(name="preferred_time3",columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType3 = TimeType.WEEKEND_MORNING;

    @Column(name="preferred_time4",columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType4 = TimeType.WEEKEND_MORNING;

    @Column(name="preferred_time5",columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType5 = TimeType.WEEKEND_MORNING;

    @Column(name="preferred_time6",columnDefinition = "ENUM('weekday_morning', 'weekday_afternoon', 'weekday_evening', 'weekend_morning', 'weekend_afternoon', 'weekend_evening') NULL DEFAULT 'weekend_morning'")
    @Builder.Default
    private TimeType timeType6 = TimeType.WEEKEND_MORNING;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;



}
