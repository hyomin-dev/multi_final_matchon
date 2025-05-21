package com.multi.matchon.matchup.controller;



import com.multi.matchon.common.auth.dto.CustomUser;

import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.matchup.service.MatchupService;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping("/matchup")
@Slf4j
@RequiredArgsConstructor
public class MatchupController {

    private final MatchupService matchupService;


    // 게시글 작성하기

    @GetMapping("/board/register")
    public ModelAndView boardRegister(ModelAndView mv){
        mv.setViewName("/matchup/matchup-board-register");
        mv.addObject("reqMatchupBoardDto",new ReqMatchupBoardDto());
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
    public ModelAndView boardDetail(@RequestParam("matchup-board-id") Long boardId, ModelAndView mv){
        log.info("matchup-board-id: {}",boardId);
        ResMatchupBoardDto resMatchupBoardDto = matchupService.findBoardByBoardId(boardId);
        mv.addObject("resMatchupBoardDto",resMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-detail");
        return mv;
    }

    // 게시글 전체 목록 조회

    @GetMapping
    public ModelAndView showMatchupListPage(ModelAndView mv){
        //PageRequest pageRequest = PageRequest.of(0,4);
        //PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findAllWithPaging(pageRequest);
        mv.setViewName("matchup/matchup-board-list");
        //mv.addObject("pageResponseDto",pageResponseDto);
        return mv;
    }

    @GetMapping("/board/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupBoardListDto>>> findAllWithPaging(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size, @RequestParam("sportsType") String sportsType, @RequestParam("region") String region, @RequestParam("date") String date ){
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

    @GetMapping("/board/my")
    public String boardMy(){
        return "matchup/matchup-board-my";
    }

    @GetMapping("/board/my/list")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResponseDto<ResMatchupBoardListDto>>> findMyAllWithPaging(@RequestParam("page") int page, @RequestParam(value="size", required = false, defaultValue = "4") int size , @AuthenticationPrincipal CustomUser user){
        PageRequest pageRequest = PageRequest.of(page,size);
        PageResponseDto<ResMatchupBoardListDto> pageResponseDto = matchupService.findMyAllWithPaging(pageRequest, user);
        return ResponseEntity.ok(ApiResponse.ok(pageResponseDto));
    }


    // 게시글 수정/삭제

    @GetMapping("/board/edit")
    public ModelAndView boardEdit(@RequestParam("boardId") Long boardId, ModelAndView mv){

        ResMatchupBoardDto resMatchupBoardDto = matchupService.findBoardByBoardId(boardId);
        mv.addObject("resMatchupBoardDto",resMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-edit");
        return mv;
    }

    @PostMapping("/board/edit")
    public ModelAndView boardEdit(@ModelAttribute ResMatchupBoardDto resMatchupBoardDto, ModelAndView mv){
        matchupService.boardEdit(resMatchupBoardDto);
        ResMatchupBoardDto updateResMatchupBoardDto = matchupService.findBoardByBoardId(resMatchupBoardDto.getBoardId());
        mv.addObject("resMatchupBoardDto", updateResMatchupBoardDto);
        mv.setViewName("matchup/matchup-board-detail");
        return mv;
    }

    @GetMapping("/board/delete")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> boardDelete(@RequestParam("boardId") Long boardId){
        matchupService.boardDelete(boardId);
        log.info("matchup 게시글 삭제 완료");
        return ResponseEntity.ok().body(ApiResponse.ok("게시글 삭제 완료"));
    }

    // 참가 요청

    @GetMapping("/request")
    public String requestRegister(@RequestParam("boardId") Long boardId){
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


    // 첨부 파일 가져오기
    @GetMapping("/attachment/file")
    public ResponseEntity<S3Resource> getAttachmentFile(@RequestParam("saved-name") String savedName) throws IOException {

        S3Resource resource = matchupService.getAttachmentFile(savedName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(savedName, StandardCharsets.UTF_8)
                        .build()
        );


        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/attachment/presigned-url")
    public ResponseEntity<ApiResponse<String>> getAttachmentUrl(@RequestParam("saved-name") String savedName) throws IOException {

        String resourceUrl = matchupService.getAttachmentURL(savedName);

        return ResponseEntity.ok().body(ApiResponse.ok(resourceUrl));

    }

}
