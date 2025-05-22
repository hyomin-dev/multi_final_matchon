package com.multi.matchon.matchup.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
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

    // 참가 요청

    @GetMapping("/request")
    public ModelAndView showMatchupRequestRegisterPage(@RequestParam("boardId") Long boardId, ModelAndView mv) throws RuntimeException {
        ReqMatchupRequestDto reqMatchupRequestDto = matchupService.findReqMatchupRequestDtoByBoardId(boardId);
        mv.addObject("reqMatchupRequestDto",reqMatchupRequestDto);
        mv.setViewName("matchup/matchup-request-register");

        return mv;
    }

    @PostMapping("/request")
    public String registerMatchupRequest(@ModelAttribute ReqMatchupRequestDto reqMatchupRequestDto, @AuthenticationPrincipal CustomUser user){

        matchupService.requestRegister(reqMatchupRequestDto, user.getMember());
        log.info("matchup request 참가 요청 완료");
        return "matchup/matchup-request-my";
    }

    // 참가 요청 상세보기

    @GetMapping("/request/detail")
    public String showMatchupRequestDetailPage(){
        return "matchup/matchup-request-detail";
    }

    // 참가 요청 목록
    @GetMapping("/request/my")
    public String requestMy(){
        return "matchup/matchup-request-my";
    }

    @GetMapping("/request/my/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupRequestListDto>>> findMyRequestAllWithPaging(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size , @AuthenticationPrincipal CustomUser user, @RequestParam("sportsType") String sportsType, @RequestParam("date") String date){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResMatchupRequestListDto> pageResponseDto = matchupService.findMyRequestAllWithPaging(pageRequest, user, sportsType, date);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }


    // 참가 요청 수정/삭제

    @GetMapping("/request/edit")
    public String requestEdit(){
        return "matchup/matchup-request-edit";
    }

    @PostMapping("/request/edit")
    public String requestEdit(@ModelAttribute ReqMatchupRequestDto reqMatchupRequestDto){
        return "matchup/matchup-request-my";
    }

    @GetMapping("/request/delete")
    public String requestDelete(){
        return "matchup/matchup-request-my";
    }

}
