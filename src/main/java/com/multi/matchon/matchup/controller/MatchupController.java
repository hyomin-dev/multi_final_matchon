package com.multi.matchon.matchup.controller;



import com.multi.matchon.common.auth.dto.CustomUser;

import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.service.MatchupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/matchup")
@Slf4j
@RequiredArgsConstructor
public class MatchupController {

    private final MatchupService matchupService;


    // 게시글 작성하기
//    @ResponseBody
//    @GetMapping("/board/register")
//    public ResponseEntity<ApiResponse> boardRegister(){
//        return ResponseEntity.ok(ApiResponse.ok("/matchup/matchup-board-register"));
//    }

    @GetMapping("/board/register")
    public ModelAndView boardRegister(ModelAndView mv){
        mv.setViewName("/matchup/matchup-board-register");
        mv.addObject("ReqMatchupBoardDto",new ReqMatchupBoardDto());
        return mv;
    }

    @PostMapping("/board/register")
    public String boardRegister(@ModelAttribute ReqMatchupBoardDto reqMatchupBoardDto, @AuthenticationPrincipal CustomUser user){
        //log.info("{}", reqMatchupBoardDto);
        matchupService.boardRegister(reqMatchupBoardDto, user);

        log.info("matchup 게시글 등록 완료");
        return "matchup/matchup-board-list";
    }

    // 게시글 상세 조회

    @GetMapping("/board/detail")
    public String boardDetail(){
        return "matchup/matchup-board-detail";
    }

    // 게시글 전체 목록 조회

    @GetMapping
    public String showMatchupListPage(){
        //matchupService.findAll();
        return "matchup/matchup-board-list";
    }

//    @GetMapping("/board/list")
//    public ResponseEntity<ApiResponse<ResMatchupBoardDto>> getBoardListTest(){
//        //PageRequest pageRequest = PageRequest.of(1,4, new Sort(Dire)
//
//    }

    @GetMapping("/board/list")
    public String getBoardListTest(){
        //PageRequest pageRequest = PageRequest.of(1,4, new Sort(Dire)
        matchupService.findAll();
        return "tmp";
    }


    // 게시글 내가 작성한 글 목록 조회

    @GetMapping("/board/my")
    public String boardMy(){
        return "matchup/matchup-board-my";
    }

    // 게시글 수정/삭제

    @GetMapping("/board/edit")
    public String boardEdit(){
        return "matchup/matchup-board-edit";
    }

    @PostMapping("/board/edit")
    public String boardEdit(String tmp){
        return "matchup/matchup-board-detail";
    }

    @GetMapping("/board/delete")
    public String boardDelete(){
        log.info("matchup 게시글 삭제 완료");
        return "matchup/matchup-board-list";
    }

    // 참가 요청

    @GetMapping("/request")
    public String requestRegister(){
        return "matchup/matchup-request-register";
    }

    @PostMapping("/request")
    public String requestRegister(String tmp){
        log.info("matchup request 참가 요청 완료");
        return "matchup/matchup-request-detail";
    }

    // 참가 요청 상세보기

    @GetMapping("/request/detail")
    public String requestDetail(){
        return "matchup/matchup-request-detail";
    }

    // 참가 요청 목록
    @GetMapping("/request/my")
    public String requestMy(){
        return "matchup/matchup-request-my";
    }

    // 참가 요청 수정/삭제

    @GetMapping("/request/edit")
    public String requestEdit(){
        return "matchup/matchup-request-edit";
    }

    @PostMapping("/request/edit")
    public String requestEdit(String tmp){
        return "matchup/matchup-request-my";
    }

    @GetMapping("/request/delete")
    public String requestDelete(){
        return "matchup/matchup-request-my";
    }

}
