package com.multi.matchon.common.jwt.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.auth.service.CustomUserDetailsService;
import com.multi.matchon.member.domain.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // accessToken 유효 → 인증 처리
            String email = jwtTokenProvider.getEmailFromToken(token);
            CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[JwtFilter] 인증 성공 - 사용자: {}", email);
        }

        else {
            // accessToken 없음 또는 만료됨 → refreshToken 확인
            String refreshToken = resolveRefreshToken(request);

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                log.info("[JwtFilter] accessToken 없거나 만료됨 → refreshToken으로 재발급");

                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                MemberRole role = jwtTokenProvider.getRoleFromToken(refreshToken);
                String newAccessToken = jwtTokenProvider.createAccessToken(email, role);

                // 쿠키 재설정
                Cookie accessCookie = new Cookie("Authorization", newAccessToken);
                accessCookie.setHttpOnly(true);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(60 * 60);
                response.addCookie(accessCookie);

                // SecurityContext 갱신
                CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 재요청 (필터 계속 진행)
                filterChain.doFilter(request, response);
                return;
            }

            // efreshToken도 없음 or 만료
            else if (!isExcludedPath(uri)) {
                log.warn("[JwtFilter] accessToken + refreshToken 모두 없음 or 만료 → 로그인 필요");
                response.sendRedirect("/login");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 헤더
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 쿠키
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Refresh-Token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isExcludedPath(String uri) {
        return uri.equals("/") ||
                uri.startsWith("/main") ||
                uri.startsWith("/login") ||
                uri.startsWith("/signup") ||
                uri.startsWith("/auth") ||
                uri.startsWith("/css") ||
                uri.startsWith("/js") ||
                uri.startsWith("/img") ||
                uri.equals("/favicon.ico");
    }
}