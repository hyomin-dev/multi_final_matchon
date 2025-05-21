package com.multi.matchon.event.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.auth.service.CustomUserDetailsService;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.event.domain.EventRegionType;
import com.multi.matchon.event.domain.EventRequest;
import com.multi.matchon.event.domain.HostProfile;
import com.multi.matchon.event.dto.req.EventCreateRequestDto;
import com.multi.matchon.event.dto.res.CalendarDayDto;
import com.multi.matchon.event.dto.res.EventSummaryDto;
import com.multi.matchon.event.repository.EventRepository;
import com.multi.matchon.event.repository.HostProfileRepository;
import com.multi.matchon.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final HostProfileRepository hostProfileRepository;

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

        List<CalendarDayDto> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            List<EventSummaryDto> dailyEvents = events.stream()
                    .filter(e -> e.getEventDate().equals(finalDate))
                    .map(e -> new EventSummaryDto(e.getEventTitle()))
                    .toList();

            days.add(new CalendarDayDto(date, ym.getMonthValue() == date.getMonthValue(), dailyEvents));
        }

        model.addAttribute("calendarDays", days);
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        return "schedule/schedule";
    }

    // GET: 대회 등록 폼
    @GetMapping("/event/new")
    @PreAuthorize("hasRole('HOST')")
    public String showEventForm(
            @RequestParam("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @AuthenticationPrincipal CustomUser customUser,
            Model model) {

        Member member = customUser.getMember();

        HostProfile hostProfile = hostProfileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("호스트 프로필이 없습니다."));

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("hostName", hostProfile.getHostName());
        return "event/register-event";
    }

    // POST: 대회 등록 처리
    @PostMapping("/event/new")
    @PreAuthorize("hasRole('HOST')")
    public String createEvent(@AuthenticationPrincipal CustomUser customUser,
                              @ModelAttribute EventCreateRequestDto dto) {

        Member member = customUser.getMember();

        HostProfile hostProfile = hostProfileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("호스트 프로필이 없습니다."));

        EventRequest event = EventRequest.builder()
                .member(member)
                .hostProfile(hostProfile)
                .eventDate(dto.getEventDate())
                .eventRegionType(dto.getEventRegionType())
                .eventTitle(dto.getEventTitle())
                .eventMethod(dto.getEventMethod())
                .eventContact(dto.getEventContact())
                .eventStatus(Status.PENDING) // 상태 기본값: PENDING
                .build();

        eventRepository.save(event);
        return "redirect:/schedule";
    }

}

