package com.multi.matchon.common.auth.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findSimpleByMemberEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자가 존재하지 않습니다."));

        // isDeleted 고려
        if (member.getIsDeleted()) {
            // 👇 로그아웃 요청일 경우엔 허용해주기
            String requestURI = RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes ?
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI() : "";

            if (!requestURI.contains("/auth/logout")) {
                throw new UsernameNotFoundException("탈퇴한 계정입니다.");
            }
        }

        return new CustomUser(member);
    }
}
