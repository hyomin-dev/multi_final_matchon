package com.multi.matchon.common.auth.controller;

import com.multi.matchon.common.auth.service.AuthService;
import com.multi.matchon.member.dto.req.HostSignupRequestDto;
import com.multi.matchon.member.dto.req.LoginRequestDto;
import com.multi.matchon.member.dto.req.UserSignupRequestDto;
import com.multi.matchon.member.dto.res.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/user")
    public ResponseEntity<String> signupUser(@Valid @RequestBody UserSignupRequestDto requestDto) {
        authService.signupUser(requestDto);
        return ResponseEntity.ok("사용자 회원가입 성공");
    }

    @PostMapping("/signup/host")
    public ResponseEntity<String> signupHost(@Valid @RequestBody HostSignupRequestDto requestDto) {
        authService.signupHost(requestDto);
        return ResponseEntity.ok("주최자 회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        TokenResponseDto tokenResponse = authService.login(requestDto);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponseDto tokenResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}
