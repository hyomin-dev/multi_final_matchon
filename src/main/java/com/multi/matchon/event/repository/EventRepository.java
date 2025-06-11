package com.multi.matchon.event.repository;


import com.multi.matchon.common.domain.Status;
import com.multi.matchon.event.domain.EventRegionType;
import com.multi.matchon.event.domain.EventRequest;
import com.multi.matchon.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<EventRequest, Long> {
    List<EventRequest> findByEventDateBetween(LocalDate start, LocalDate end);

    List<EventRequest> findByEventDateBetweenAndEventRegionType(LocalDate start, LocalDate end, EventRegionType region);

    List<EventRequest> findByMember(Member member);

    Page<EventRequest> findByMember(Member member, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE EventRequest e SET e.eventStatus = :status WHERE e.id = :eventId")
    void updateEventStatus(@Param("eventId") Long eventId, @Param("status") Status status);


}
