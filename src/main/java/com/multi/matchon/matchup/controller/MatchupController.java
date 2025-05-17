package com.multi.matchon.matchup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/matchup")
@Slf4j
public class MatchupController {


    // 게시글 작성하기

    @GetMapping("/board/register")
    public String register(){
        return "matchup/matchup-board-register";
    }

    @PostMapping("/board/register")
    public String register(String tmp){
        log.info("matchup 게시글 등록 완료");
        return "matchup/matchup-list";
    }

    // 게시글 상세 조회

    @GetMapping("/board/detail")
    public String detail(){
        return "matchup/matchup-board-detail";
    }

    // 게시글 전체 목록 조회

    @GetMapping
    public String listPage(){
        return "matchup/matchup-board-list";
    }

    // 게시글 내가 작성한 글 목록 조회

    @GetMapping("/board/my")
    public String my(){
        return "matchup/matchup-board-my";
    }

    // 게시글 수정/삭제

    @GetMapping("/board/edit")
    public String edit(){
        return "matchup/matchup-board-edit";
    }

    @PostMapping("/board/edit")
    public String edit(String tmp){
        return "matchup/matchup-board-detail";
    }

    @GetMapping("/board/delete")                                                   
    public String delete(){
        log.info("matchup 게시글 삭제 완료");
        return "matchup/matchup-list";
    }

    // 참가 요청

    @GetMapping("/request")
    public String request(){
        return "matchup/matchup-request-register";
    }

    @PostMapping("/request")
    public String request(String tmp){
        log.info("matchup request 참가 요청 완료");
        return "matchup/matchup-request-detail";
    }

    // 참가 요청 상세보기



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
