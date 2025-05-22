package com.multi.matchon.member.controller;

import com.multi.matchon.common.domain.Status;
import com.multi.matchon.event.domain.EventRequest;
import com.multi.matchon.event.dto.res.ResMyEventDto;
import com.multi.matchon.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EventRepository eventRepository;

    @GetMapping
    public String adminHome() {
        return "admin/admin-home";
    }


    @GetMapping(value = "/event", produces = MediaType.TEXT_HTML_VALUE)
    public String getAdminEventPage(Model model) {
        return "admin/admin-event-list";
    }


    @ResponseBody
    @GetMapping("/event/page")
    public Page<ResMyEventDto> getEventPage(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdDate").descending());
        Page<EventRequest> eventPage = eventRepository.findAll(pageable);

        return eventPage.map(e -> new ResMyEventDto(
                e.getId(),
                e.getEventTitle(),
                e.getMember().getMemberName(),
                e.getEventStatus(),
                e.getCreatedDate()
        ));
    }


    @GetMapping("/event/{id}")
    public String adminEventDetail(@PathVariable Long id, Model model) {
        EventRequest event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대회를 찾을 수 없습니다."));

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
        model.addAttribute("statusLabel", statusLabel);
        model.addAttribute("isAdmin", true);

        return "admin/admin-event-detail";
    }

    @PostMapping("/event/{id}/status")
    @Transactional
    public String updateEventStatus(@PathVariable Long id, @RequestParam("status") Status status) {
        eventRepository.updateEventStatus(id, status);
        return "redirect:/admin/event";
    }
}