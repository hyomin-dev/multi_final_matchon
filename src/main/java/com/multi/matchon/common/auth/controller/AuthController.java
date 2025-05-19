package com.multi.matchon.common.auth.controller;

import com.multi.matchon.common.auth.service.AuthService;
import com.multi.matchon.common.jwt.repository.RefreshTokenRepository;
import com.multi.matchon.common.jwt.service.JwtTokenProvider;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.dto.req.LoginRequestDto;
import com.multi.matchon.member.dto.req.SignupRequestDto;
import com.multi.matchon.member.dto.res.TokenResponseDto;
import com.multi.matchon.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/signup/user")
    public ResponseEntity<String> signupUser(@RequestBody SignupRequestDto requestDto) {
        authService.signupUser(requestDto);
        return ResponseEntity.ok("사용자 회원가입 성공");
    }

    @PostMapping("/signup/host")
    public ResponseEntity<String> signupHost(@RequestBody SignupRequestDto requestDto) {
        authService.signupHost(requestDto);
        return ResponseEntity.ok("주최자 회원가입 성공");
    }


    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        TokenResponseDto tokenResponse = authService.login(requestDto);
        ResponseCookie accessTokenCookie = ResponseCookie.from("Authorization",tokenResponse.getAccessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,accessTokenCookie.toString());
        return ResponseEntity.ok().headers(headers).body(tokenResponse);

        //return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "Authorization", required = false) String token) {
        if (token != null) {

            // 토큰에서 이메일 추출
            String email = jwtTokenProvider.getEmailFromToken(token);

            // 이메일로 사용자 조회
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

            // DB의 RefreshToken 삭제
            refreshTokenRepository.findByMember(member)
                    .ifPresent(refreshTokenRepository::delete);

            // accessToken 쿠키 제거
            ResponseCookie deleteCookie = ResponseCookie.from("Authorization", "")
                    .path("/")
                    .maxAge(0)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body("로그아웃 완료");
        }

        return ResponseEntity.badRequest().body("토큰이 없습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponseDto tokenResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}
