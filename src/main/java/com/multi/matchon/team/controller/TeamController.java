package com.multi.matchon.team.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.dto.res.ResTeamDto;
import com.multi.matchon.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/team")
@Slf4j
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

//    @GetMapping
//    public String memberAllList(){
//        //matchupService.findAll();
//        return "team/team-list";
//    }

    @GetMapping("/team/register")
    public ModelAndView teamRegister(ModelAndView mv){
        mv.setViewName("/team/team-register");
        mv.addObject("ReqTeamDto",new ReqTeamDto());
        return mv;
    }
    @PostMapping("/team/register")
    public String teamRegister(@ModelAttribute ReqTeamDto reqTeamDto, @AuthenticationPrincipal CustomUser user){
        teamService.teamRegister(reqTeamDto, user);

        log.info("team 등록 완료");
        return "team/team-list";
    }
    @GetMapping
    public ModelAndView showTeamListPage(ModelAndView mv){
        //PageRequest pageRequest = PageRequest.of(0,4);
        //PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findAllWithPaging(pageRequest);
        mv.setViewName("team/team-list");
        //mv.addObject("pageResponseDto",pageResponseDto);
        return mv;
    }

    @GetMapping("/team/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResTeamDto>>> findAllWithPaging(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "5") int size, @RequestParam("recruitingPosition") String recruitingPosition, @RequestParam("region") String region, @RequestParam("teamRatingAverage") Double teamRatingAverage ){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResTeamDto> pageResponseDto = teamService.findAllWithPaging(pageRequest, recruitingPosition, region, teamRatingAverage);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }

}


