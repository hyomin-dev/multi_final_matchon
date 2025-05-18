package com.multi.matchon.common.jwt.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.auth.service.CustomUserDetailsService;
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

        // 쿠키에서 찾기
        if(token==null){
            if(request.getCookies()!=null){
                for(Cookie cookie : request.getCookies()){
                    if("Authorization".equals(cookie.getName())){
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 토큰이 없고 인증 제외 경로가 아닌 경우 : 직접 401 응답 주고 끝
        if (token == null && !isExcludedPath(uri)) {
            log.warn("[JwtFilter] 인증이 필요한 경로인데 토큰이 없습니다. uri: {}", uri);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"로그인이 필요합니다.\"}");
            return;
        }

        // 토큰이 존재하는 경우 검증 및 인증 정보 설정
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[JwtFilter] 인증 성공: {}", email);
        }

        // 인증 제외 경로거나 정상 인증된 경우 계속 진행
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