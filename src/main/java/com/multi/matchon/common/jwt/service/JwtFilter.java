package com.multi.matchon.common.jwt.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // 1. 토큰이 없고 인증 제외 경로일 경우: 그냥 통과
        if (token == null && isExcludedPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 토큰이 있을 경우 → 유효성 검증 및 SecurityContext 설정
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 토큰이 없고 인증 제외 경로도 아닌 경우 → SecurityContext에 인증 정보 없음
        // 나중에 AuthenticationEntryPoint가 401 처리함 (정상 동작)

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 인증 없이 접근 허용할 경로 정의
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