package com.multi.matchon.stadium.controller;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumService stadiumService;

    @GetMapping("/stadium")
    public String showStadiumList(@RequestParam(required = false) String region, Model model) {
        List<Stadium> stadiums;

        if (region != null && !region.isEmpty()) {
            stadiums = stadiumService.getStadiumsByRegion(region);
        } else {
            stadiums = stadiumService.getAllStadiums();
        }

        model.addAttribute("stadiums", stadiums);
        model.addAttribute("selectedRegion", region);
        return "stadium/stadium";
    }
}
