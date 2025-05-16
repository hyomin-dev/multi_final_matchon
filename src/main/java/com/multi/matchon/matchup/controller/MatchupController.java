package com.multi.matchon.matchup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/matchup")
public class MatchupController {

    @GetMapping
    public String listPage(){
        return "matchup/matchup_list";
    }

}
