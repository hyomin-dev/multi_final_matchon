package com.multi.matchon.stadium.controller;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumService stadiumService;

    @GetMapping("/stadium")
    public String showStadiumList(@RequestParam(required = false) String region, @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Stadium> stadiums;

        if (region != null && !region.isEmpty()) {
            stadiums = stadiumService.getStadiumsByRegion(region, pageable);
        } else {
            stadiums = stadiumService.getAllStadiums(pageable);
        }

        model.addAttribute("stadiums", stadiums);
        model.addAttribute("selectedRegion", region);
        return "stadium/stadium";
    }

    @GetMapping("/stadium/{id}")
    public String showStadiumDetail(@PathVariable Long id, Model model) {
        Stadium stadium = stadiumService.getStadiumById(id);
        model.addAttribute("stadium", stadium);
        return "stadium/stadium-detail"; // 구장 상세보기 페이지로 이동.
    }

    @GetMapping("/stadium/map")
    public String showStadiumMap(@RequestParam("id") Long id, Model model) {
        Stadium stadium = stadiumService.getStadiumById(id);
        model.addAttribute("stadium", stadium);
        return "stadium/stadium-map";
    }

}
