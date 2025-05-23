package com.multi.matchon.guest.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="guest_request")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class GuestRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="guest_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="guest_board_id", nullable = false)
    private GuestBoard guestBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="applicant_id",nullable = false)
    private Member member;

    @Column(name="reservation_attachment_enabled",nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE CHECK (reservation_attachment_enabled=true)")
    private Boolean reservationAttachmentEnabled;

    @Column(name="self_intro",nullable = false,columnDefinition = "TEXT")
    private String selfIntro;

    @Column(name="sports_facility_name",nullable = false,columnDefinition = "VARCHAR(100)")
    private String sportsFacilityName;

    @Column(name="sports_facility_address",nullable = false,columnDefinition = "VARCHAR(100)")
    private String sportsFacilityAddress;

    @Column(name="match_date",nullable = false)
    private LocalDateTime matchDate;

    @Column(name="match_duration",nullable = false)
    private LocalTime matchDuration;

    @Column(name="match_description",nullable = false,columnDefinition = "TEXT")
    private String matchDescription;

    @Column(name="status",nullable = false, columnDefinition = "ENUM('PENDING', 'APPROVED', 'DENIED', 'CANCELREQUESTED') NOT NULL DEFAULT 'PENDING'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status guestStatus = Status.PENDING;

    @Column(name="request_resubmitted_count")
    @Builder.Default
    private Integer guestRequestResubmittedCount = 0;

    @Column(name="cancel_resubmitted_count")
    @Builder.Default
    private Integer guestCancelResubmittedCount = 0;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;

}
