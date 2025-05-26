package com.multi.matchon.common.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.PositionName;
import com.multi.matchon.common.domain.TimeType;
import com.multi.matchon.common.service.MypageService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MemberService memberService;
    private final MypageService mypageService;

    @GetMapping
    @Transactional(readOnly = true)
    public String getMypage(@AuthenticationPrincipal CustomUser user, Model model) {
        Member member = memberService.findForMypage(user.getUsername());
        Map<String, Object> data = mypageService.getMypageInfo(member);
        data.forEach(model::addAttribute);

        model.addAttribute("myPosition", member.getPositions() != null ? member.getPositions().getPositionName().name() : "");
        model.addAttribute("myTimeType", member.getTimeType() != null ? member.getTimeType().name() : "");
        return "mypage/mypage";
    }

    @PostMapping("/uploadProfile")
    public String uploadProfile(@AuthenticationPrincipal CustomUser user,
                                @RequestParam MultipartFile profileImage) {
        Member member = memberService.findForMypage(user.getUsername());
        mypageService.uploadProfileImage(member, profileImage);
        return "redirect:/mypage";
    }

    @PostMapping("/hostName")
    public ResponseEntity<String> updateHostName(@AuthenticationPrincipal CustomUser user,
                                                 @RequestParam String hostName) {
        Member member = memberService.findForMypage(user.getUsername());
        mypageService.updateHostName(member, hostName);
        return ResponseEntity.ok("기관명 저장 완료");
    }

    @GetMapping("/enums")
    @ResponseBody
    public Map<String, Object> getEnums() {
        Map<String, Object> result = new HashMap<>();
        result.put("positionNames", Arrays.stream(PositionName.values()).map(Enum::name).toList());
        result.put("timeTypes", Arrays.stream(TimeType.values()).map(Enum::name).toList());
        return result;
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateMypage(@AuthenticationPrincipal CustomUser user,
                                               @RequestBody Map<String, Object> payload) {
        try {
            PositionName positionName = PositionName.valueOf((String) payload.get("positionName"));
            TimeType timeType = TimeType.valueOf((String) payload.get("timeType"));
            Double temperature = Double.valueOf(payload.get("temperature").toString());

            mypageService.updateMypage(user.getUsername(), positionName, timeType, temperature);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            log.error("마이페이지 수정 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정 실패: " + e.getMessage());
        }
    }
}
