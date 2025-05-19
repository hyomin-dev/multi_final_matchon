package com.multi.matchon.team.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/team")
@Slf4j
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public String memberAllList(){
        //matchupService.findAll();
        return "team/team-list";
    }

    @GetMapping("/team/register")
    public ModelAndView teamRegister(ModelAndView mv){
        mv.setViewName("/team/team-register");
        mv.addObject("ReqTeamDto",new ReqTeamDto());
        return mv;
    }
    @PostMapping("/team/register")
    public String teamRegister(@ModelAttribute ReqTeamDto reqTeamDto, @AuthenticationPrincipal CustomUser user){
        //log.info("{}", reqMatchupBoardDto);
//        teamService.teamRegister(reqTeamDto, user);

        log.info("matchup 게시글 등록 완료");
        return "matchup/matchup-board-list";
    }

}
