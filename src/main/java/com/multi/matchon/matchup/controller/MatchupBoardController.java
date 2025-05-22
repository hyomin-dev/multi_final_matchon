package com.multi.matchon.matchup.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
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
@RequestMapping("/matchup/board")
@Slf4j
@RequiredArgsConstructor
public class MatchupBoardController {

    private final MatchupService matchupService;

    // 게시글 작성하기

    @GetMapping("/register")
    public ModelAndView showMatchupBoardRegisterPage (ModelAndView mv){
        mv.setViewName("/matchup/matchup-board-register");
        mv.addObject("reqMatchupBoardDto",new ReqMatchupBoardDto());
        return mv;
    }

    @PostMapping("/register")
    public String registerMatchupBoard(@ModelAttribute ReqMatchupBoardDto reqMatchupBoardDto, @AuthenticationPrincipal CustomUser user){
        //log.info("{}", reqMatchupBoardDto);
        matchupService.boardRegister(reqMatchupBoardDto, user);

        log.info("matchup 게시글 등록 완료");
        return "matchup/matchup-board-list";
    }

    // 게시글 상세 조회

    @GetMapping("/detail")
    public ModelAndView getMatchupBoardDetail(@RequestParam("matchup-board-id") Long boardId, ModelAndView mv){
        log.info("matchup-board-id: {}",boardId);
        ResMatchupBoardDto resMatchupBoardDto = matchupService.findBoardByBoardId(boardId);
        mv.addObject("resMatchupBoardDto",resMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-detail");
        return mv;
    }

    // 게시글 전체 목록 조회

    @GetMapping
    public ModelAndView showMatchupBoardPage(ModelAndView mv){
        //PageRequest pageRequest = PageRequest.of(0,4);
        //PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findAllWithPaging(pageRequest);
        mv.setViewName("matchup/matchup-board-list");
        //mv.addObject("pageResponseDto",pageResponseDto);
        return mv;
    }

    @GetMapping("/board/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupBoardListDto>>> listMatchupBoardByFilter(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size, @RequestParam("sportsType") String sportsType, @RequestParam("region") String region, @RequestParam("date") String date ){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findAllWithPaging(pageRequest, sportsType, region, date);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }

//    @GetMapping("/board/listtest")
//    public String findBoardListTest(){
//        //PageRequest pageRequest = PageRequest.of(1,4, new Sort(Dire)
//        matchupService.findBoardListTest();
//        return "tmp";
//    }

    // 게시글 내가 작성한 글 목록 조회

    @GetMapping("/my")
    public String showMyBoardPage(){
        return "matchup/matchup-board-my";
    }

    @GetMapping("/board/my/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupBoardListDto>>> listMyMatchupBoardByFilter(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size , @AuthenticationPrincipal CustomUser user){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findMyAllWithPaging(pageRequest, user);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }

    // 게시글 수정/삭제

    @GetMapping("/board/edit")
    public ModelAndView showMatchupBoardEditPage(@RequestParam("boardId") Long boardId, ModelAndView mv){

        ResMatchupBoardDto resMatchupBoardDto = matchupService.findBoardByBoardId(boardId);
        mv.addObject("resMatchupBoardDto",resMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-edit");
        return mv;
    }

    @PostMapping("/board/edit")
    public ModelAndView editMatchupBoard(@ModelAttribute ResMatchupBoardDto resMatchupBoardDto, ModelAndView mv){
        matchupService.boardEdit(resMatchupBoardDto);
        ResMatchupBoardDto updateResMatchupBoardDto = matchupService.findBoardByBoardId(resMatchupBoardDto.getBoardId());
        mv.addObject("resMatchupBoardDto", updateResMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-detail");
        return mv;
    }

    @GetMapping("/board/delete")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> deleteBoard(@RequestParam("boardId") Long boardId){
        matchupService.boardDelete(boardId);
        log.info("matchup 게시글 삭제 완료");
        return ResponseEntity.ok().body(ApiResponse.ok("게시글 삭제 완료"));
    }

}
