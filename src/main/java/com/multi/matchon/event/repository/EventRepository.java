package com.multi.matchon.event.repository;

import com.multi.matchon.event.domain.EventRegionType;
import com.multi.matchon.event.domain.EventRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<EventRequest, Long> {
    List<EventRequest> findByEventDateBetween(LocalDate start, LocalDate end);

    List<EventRequest> findByEventDateBetweenAndEventRegionType(LocalDate start, LocalDate end, EventRegionType region);
}
