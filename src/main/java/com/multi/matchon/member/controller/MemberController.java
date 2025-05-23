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
    @GetMapping("/search/teamname")
    public ResponseEntity<ApiResponse<String>> getTeamNameByMemberEmail(@AuthenticationPrincipal CustomUser user){
        String teamName = memberService.getTeamNameByMemberEmail(user.getMember().getMemberEmail());
        return ResponseEntity.ok().body(ApiResponse.ok(teamName));
    }


    @ResponseBody
    @GetMapping("/search/manner-temperature")
    public ResponseEntity<ApiResponse<Double>> getTemperatureByMemberEmail(@AuthenticationPrincipal CustomUser user){
        Double myTemperature = memberService.getTemperatureByMemberEmail(user.getMember().getMemberEmail());
        return ResponseEntity.ok().body(ApiResponse.ok(myTemperature));
    }
}
