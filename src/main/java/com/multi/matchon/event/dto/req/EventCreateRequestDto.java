package com.multi.matchon.event.dto.req;

import com.multi.matchon.event.domain.EventRegionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EventCreateRequestDto {
    private LocalDate eventDate;
    private EventRegionType eventRegionType;
    private String eventTitle;
    private String eventMethod;
    private String eventContact;
}
