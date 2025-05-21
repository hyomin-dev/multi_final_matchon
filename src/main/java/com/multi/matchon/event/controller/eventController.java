package com.multi.matchon.event.controller;

import com.multi.matchon.event.domain.EventRegionType;
import com.multi.matchon.event.domain.EventRequest;
import com.multi.matchon.event.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class eventController {

    private final EventRepository eventRepository;

    @GetMapping("/schedule")
    public String getSchedule(@RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month,
                              @RequestParam(required = false) EventRegionType region, Model model) {
        LocalDate today = LocalDate.now();
        int y = (year != null) ? year : today.getYear();
        int m = (month != null) ? month : today.getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDate start = ym.atDay(1).with(DayOfWeek.SUNDAY);
        LocalDate end = ym.atEndOfMonth().with(DayOfWeek.SATURDAY);

        List<EventRequest> events = (region != null)
                ? eventRepository.findByEventDateBetweenAndEventRegionType(start, end, region)
                : eventRepository.findByEventDateBetween(start, end);

        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            List<EventSummary> dailyEvents = events.stream()
                    .filter(e -> e.getEventDate().equals(finalDate))
                    .map(e -> new EventSummary(e.getEventTitle(), colorForRegion(e.getEventRegionType())))
                    .toList();

            days.add(new CalendarDay(date, ym.getMonthValue() == date.getMonthValue(), dailyEvents));
        }

        model.addAttribute("calendarDays", days);
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        return "schedule/schedule";
    }

    private String colorForRegion(EventRegionType region) {
        return switch (region) {
            case CAPITAL_REGION -> "#f28b82"; // example colors
            case YEONGNAM_REGION -> "#fbbc04";
            case HONAM_REGION -> "#34a853";
            case CHUNGCHEONG_REGION -> "#4285f4";
            case GANGWON_REGION -> "#9c27b0";
            case JEJU -> "#ff6d00";
        };
    }

    @Getter
    @AllArgsConstructor
    public static class CalendarDay {
        private LocalDate date;
        private boolean currentMonth;
        private List<EventSummary> events;

        public int getDayOfMonth() {
            return date.getDayOfMonth();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class EventSummary {
        private String eventTitle;
        private String regionColor;
    }
}

