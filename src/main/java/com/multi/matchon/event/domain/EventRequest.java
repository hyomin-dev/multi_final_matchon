package com.multi.matchon.event.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="event_request")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class EventRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="host_id", nullable = false)
    private Member member;

    @Column(name="event_date",nullable = false)
    private LocalDate eventDate;

    @Column(name="event_region",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventRegionType eventRegionType;

    @Column(name="event_title",nullable = false, columnDefinition = "VARCHAR(100)")
    private String eventTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="host_profile_id",nullable = false)
    private HostProfile hostProfile;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sports_type_id",nullable = false)
    private SportsType sportsType;

    @Column(name="event_method", nullable = false,columnDefinition = "VARCHAR(100)")
    private String eventMethod;

    @Column(name="event_contact",nullable = false, columnDefinition = "VARCHAR(50)")
    private String eventContact;

    @Column(name="event_status",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventStatus eventStatus;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;


}
