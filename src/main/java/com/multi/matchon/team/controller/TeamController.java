package com.multi.matchon.team.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.team.dto.req.ReqReviewDto;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.dto.req.ReqTeamJoinDto;
import com.multi.matchon.team.dto.res.ResReviewDto;
import com.multi.matchon.team.dto.res.ResTeamDto;
import com.multi.matchon.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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
    public ResponseEntity<ApiResponse<PageResponseDto<ResTeamDto>>> findAllWithPaging(
            @RequestParam("page") int page,
            @RequestParam(value="size", required = false, defaultValue = "5") int size,
            @RequestParam(value = "recruitingPosition", required = false) String recruitingPosition,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "teamRatingAverage", required = false) Double teamRatingAverage
    ){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResTeamDto> pageResponseDto = teamService.findAllWithPaging(pageRequest, recruitingPosition, region, teamRatingAverage);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }

    @GetMapping("/team/{teamId}")
    public ModelAndView viewTeamDetail(@PathVariable Long teamId) {
        ModelAndView mv = new ModelAndView("team/team-detail");

        ResTeamDto teamDto = teamService.findTeamById(teamId);
        mv.addObject("team", teamDto);

        return mv;
    }

    @PostMapping("/join/{teamId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> joinTeam(
            @PathVariable Long teamId,
            @RequestBody ReqTeamJoinDto joinRequest,
            @AuthenticationPrincipal CustomUser user) {
        
        try {
            teamService.processTeamJoinRequest(teamId, joinRequest, user);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }


    }
    @PostMapping("/team/{teamId}/review")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> submitReview(
            @PathVariable Long teamId,
            @RequestBody ReqReviewDto reviewDto,
            @AuthenticationPrincipal CustomUser user) {

        teamService.saveReview(teamId, user, reviewDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/team/{teamId}/reviews")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ResReviewDto>>> getReviews(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getReviewsForTeam(teamId)));
    }

    @PutMapping("/team/review/{reviewId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReqReviewDto reviewDto,
            @AuthenticationPrincipal CustomUser user) {
        
        teamService.updateReview(reviewId, user, reviewDto);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/team/review/{reviewId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUser user) {
        
        teamService.deleteReview(reviewId, user);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}


