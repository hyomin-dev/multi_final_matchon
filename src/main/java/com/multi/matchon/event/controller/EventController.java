package com.multi.matchon.event.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.event.domain.EventRegionType;
import com.multi.matchon.event.domain.EventRequest;
import com.multi.matchon.event.domain.HostProfile;
import com.multi.matchon.event.dto.req.EventReqDto;
import com.multi.matchon.event.dto.res.CalendarDayDto;
import com.multi.matchon.event.dto.res.EventSummaryDto;
import com.multi.matchon.event.dto.res.EventResDto;
import com.multi.matchon.event.repository.EventRepository;
import com.multi.matchon.event.repository.HostProfileRepository;
import com.multi.matchon.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.multi.matchon.common.domain.Status.*;

@Controller
@RequiredArgsConstructor
public class EventController {

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

        // 승인된 일정만 필터링
        List<EventRequest> approvedEvents = events.stream()
                .filter(e -> e.getEventStatus() == Status.APPROVED)
                .toList();

        List<CalendarDayDto> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            List<EventSummaryDto> dailyEvents = approvedEvents.stream()
                    .filter(e -> e.getEventDate().equals(finalDate))
                    .map(e -> new EventSummaryDto( e.getId(),
                            e.getEventTitle(),
                            e.getEventRegionType().name(),
                            e.getHostProfile().getHostName(),
                            e.getEventAddress(),
                            e.getEventMethod(),
                            e.getEventContact()))
                    .toList();

            days.add(new CalendarDayDto(date, ym.getMonthValue() == date.getMonthValue(), dailyEvents));
        }

        model.addAttribute("calendarDays", days);
        model.addAttribute("events", approvedEvents);
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        return "event/schedule";
    }

    // GET: 대회 등록 폼
    @GetMapping("/event/new")
    @PreAuthorize("hasRole('HOST')")
    public String showEventForm(
            @RequestParam("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @AuthenticationPrincipal CustomUser customUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        Member member = customUser.getMember();

        Optional<HostProfile> optionalHost = hostProfileRepository.findByMember(member);
        if (optionalHost.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ 주최기관이 미등록 상태입니다. 마이페이지에서 먼저 등록해주세요.");
            return "redirect:/mypage";
        }

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("hostName", optionalHost.get().getHostName());
        return "event/event-register";
    }

    // POST: 대회 등록 처리
    @PostMapping("/event/new")
    @PreAuthorize("hasRole('HOST')")
    public String createEvent(@AuthenticationPrincipal CustomUser customUser,
                              @ModelAttribute EventReqDto dto,
                              RedirectAttributes redirectAttributes) {

        Member member = customUser.getMember();

        Optional<HostProfile> optionalHost = hostProfileRepository.findByMember(member);
        if (optionalHost.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ 주최기관이 없습니다.");
            return "redirect:/mypage";
        }

        HostProfile hostProfile = optionalHost.get();

        EventRequest event = EventRequest.builder()
                .member(member)
                .hostProfile(hostProfile)
                .eventDate(dto.getEventDate())
                .eventRegionType(dto.getEventRegionType())
                .eventTitle(dto.getEventTitle())
                .eventAddress(dto.getEventAddress())
                .eventMethod(dto.getEventMethod())
                .eventContact(dto.getEventContact())
                .eventStatus(Status.PENDING)
                .build();

        eventRepository.save(event);
        return "redirect:/schedule";
    }

    @GetMapping(value = "/event/my", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('HOST')")
    public String getMyEvents(@AuthenticationPrincipal CustomUser customUser,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              HttpServletRequest request) {

        Member member = customUser.getMember();
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdDate").descending());
        Page<EventRequest> eventPage = eventRepository.findByMember(member, pageable);

        Page<EventResDto> dtoPage = eventPage.map(e -> new EventResDto(
                e.getId(),
                e.getEventTitle(),
                e.getMember().getMemberName(),
                e.getEventStatus(),
                e.getCreatedDate()
        ));

        model.addAttribute("myEvents", dtoPage);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "event/event-list :: #eventTableContainer";
        }

        return "event/event-list";
    }

    @GetMapping("/event/{id}")
    public String getEventDetail(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUser customUser,
                                 Model model) {
        EventRequest event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대회를 찾을 수 없습니다."));

        boolean isOwner = customUser != null && customUser.getMember().getId().equals(event.getMember().getId());
        boolean isApproved = event.getEventStatus() == Status.APPROVED;

        if (!isOwner && !isApproved) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        String regionLabel = switch (event.getEventRegionType()) {
            case CAPITAL_REGION -> "수도권";
            case YEONGNAM_REGION -> "영남권";
            case HONAM_REGION -> "호남권";
            case CHUNGCHEONG_REGION -> "충청권";
            case GANGWON_REGION -> "강원권";
            case JEJU -> "제주권";
        };


        String statusLabel = switch (event.getEventStatus()) {
            case PENDING -> "대기중";
            case APPROVED -> "승인";
            case DENIED -> "반려";
            default -> "알 수 없음";
        };

        model.addAttribute("event", event);
        model.addAttribute("regionLabel", regionLabel);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("statusLabel", statusLabel);
        return "event/event-detail";
    }

    @PostMapping("/event/delete/{id}")
    @PreAuthorize("hasRole('HOST')")
    public String deleteEvent(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUser customUser) {
        EventRequest event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대회를 찾을 수 없습니다."));

        if (!event.getMember().getId().equals(customUser.getMember().getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 반려 상태일 경우에만 삭제 가능
        if (event.getEventStatus() != Status.DENIED) {
            throw new IllegalStateException("반려된 대회만 삭제할 수 있습니다.");
        }

        eventRepository.delete(event);
        return "redirect:/event/my";
    }

    // 대회 등록
    @GetMapping("/api/events")
    @ResponseBody
    public List<CalendarDayDto> getApprovedEvents(@RequestParam int year, @RequestParam int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1).with(DayOfWeek.SUNDAY);
        LocalDate end = ym.atEndOfMonth().with(DayOfWeek.SATURDAY);

        List<EventRequest> events = eventRepository.findByEventDateBetween(start, end);
        List<EventRequest> approved = events.stream()
                .filter(e -> e.getEventStatus() == Status.APPROVED)
                .toList();

        List<CalendarDayDto> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            List<EventSummaryDto> summaries = approved.stream()
                    .filter(e -> e.getEventDate().equals(finalDate))
                    .map(e -> new EventSummaryDto( e.getId(),
                            e.getEventTitle(),
                            e.getEventRegionType().name(),
                            e.getHostProfile().getHostName(),
                            e.getEventAddress(),
                            e.getEventMethod(),
                            e.getEventContact()))
                    .toList();
            days.add(new CalendarDayDto(date, ym.getMonthValue() == date.getMonthValue(), summaries));
        }

        return days;
    }

}