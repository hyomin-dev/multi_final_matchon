package com.multi.matchon.member.service;

import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findForMypage(String email) {
        return memberRepository.findForMypage(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    public String searchByEmail(String email) {
        Member member = memberRepository.findByEmailWithTeam(email).orElseThrow(() -> new IllegalArgumentException("잘못된 종목 ID입니다."));

        return member.getTeam().getTeamName();
    }
}
