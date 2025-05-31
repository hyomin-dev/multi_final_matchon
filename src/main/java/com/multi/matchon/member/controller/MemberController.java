package com.multi.matchon.member.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @ResponseBody
    @GetMapping("/search/team-name")
    public ResponseEntity<ApiResponse<String>> getTeamNameByMember(@AuthenticationPrincipal CustomUser user){
        String teamName = memberService.findTeamNameByMember(user.getMember());
        return ResponseEntity.ok().body(ApiResponse.ok(teamName));
    }


    @ResponseBody
    @GetMapping("/search/my-temperature")
    public ResponseEntity<ApiResponse<Double>> getMyTemperatureByMember(@AuthenticationPrincipal CustomUser user){
        Double myTemperature = memberService.findMyTemperatureByMember(user.getMember());
        return ResponseEntity.ok().body(ApiResponse.ok(myTemperature));
    }
}
