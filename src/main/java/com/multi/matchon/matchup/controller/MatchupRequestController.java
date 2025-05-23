package com.multi.matchon.matchup.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import com.multi.matchon.matchup.service.MatchupRequestService;
import com.multi.matchon.matchup.service.MatchupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/matchup/request")
@Slf4j
@RequiredArgsConstructor
public class MatchupRequestController {

    private final MatchupService matchupService;
    private final MatchupRequestService matchupRequestService;

    // 참가 요청

    @GetMapping()
    public ModelAndView showMatchupRequestRegisterPage(@RequestParam("boardId") Long boardId, ModelAndView mv) throws RuntimeException {
        ReqMatchupRequestDto reqMatchupRequestDto = matchupRequestService.findReqMatchupRequestDtoByBoardId(boardId);
        mv.addObject("reqMatchupRequestDto",reqMatchupRequestDto);
        mv.setViewName("matchup/matchup-request-register");

        return mv;
    }

    @PostMapping()
    public String registerMatchupRequest(@ModelAttribute ReqMatchupRequestDto reqMatchupRequestDto, @AuthenticationPrincipal CustomUser user){

        matchupRequestService.registerMatchupRequest(reqMatchupRequestDto, user);
        log.info("matchup request 참가 요청 완료");
        return "matchup/matchup-request-my";
    }

    // 참가 요청 상세보기

    @GetMapping("/detail")
    public ModelAndView requestDetail(@RequestParam("request-id") Long requestId, ModelAndView mv){

        ResMatchupRequestDto resMatchupRequestDto = matchupRequestService.findResMatchRequestDtoByRequestId(requestId);
        mv.addObject("resMatchupRequestDto",resMatchupRequestDto);
        mv.setViewName("matchup/matchup-request-detail");
        return mv;
    }

    // 참가 요청 목록
    @GetMapping("/my")
    public String showMyMatchupRequestPage(){
        return "matchup/matchup-request-my";
    }

    @GetMapping("/my/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupRequestListDto>>> listMyMatchupRequestByFilter(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size , @AuthenticationPrincipal CustomUser user, @RequestParam("sportsType") String sportsType, @RequestParam("date") String date){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResMatchupRequestListDto> pageResponseDto = matchupRequestService.findAllMyMatchupRequestWithPaging(pageRequest, user, sportsType, date);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }

    // 참가 요청 수정/삭제

    @GetMapping("/edit")
    public ModelAndView showMatchupRequestEditPage(@RequestParam("request-id") Long requestId, @AuthenticationPrincipal CustomUser user, ModelAndView mv){
        ResMatchupRequestDto resMatchupRequestDto = matchupRequestService.findResMatchRequestDtoByRequestId(requestId);
        mv.addObject("resMatchupRequestDto",resMatchupRequestDto);
        mv.setViewName("matchup/matchup-request-edit");
        return mv;
    }

<<<<<<< HEAD
//    @PostMapping("/edit")
//    public String editMatchupRequest(@ModelAttribute ResMatchupRequestDto resMatchupRequestDto, @RequestParam("request-id") Long requestId){
//        matchupRequestService.updateMatchupRequest(resMatchupRequestDto, requestId);
//        return "matchup/matchup-request-my";
//    }
=======
    @PostMapping("/edit")
    public String editMatchupRequest(@ModelAttribute ResMatchupRequestDto resMatchupRequestDto, @RequestParam("request-id") Long requestId){
        matchupRequestService.updateMatchupRequest(resMatchupRequestDto, requestId);
        return "matchup/matchup-request-my";
    }
>>>>>>> develop

    @GetMapping("/delete")
    public String deleteMatchupRequest(){
        return "matchup/matchup-request-my";
    }

}
