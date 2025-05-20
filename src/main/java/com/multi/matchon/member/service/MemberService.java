package com.multi.matchon.member.service;

import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findForMypage(String email) {
        return memberRepository.findForMypage(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public String getTeamNameByMemberEmail(String email) {
        Member member = memberRepository.findByMemberEmailWithTeam(email).orElseThrow(() -> new IllegalArgumentException("이 email을 가진 회원은 소속팀이 없습니다."));

        return member.getTeam().getTeamName();
    }

    @Transactional(readOnly = true)
    public Double getTemperatureByMemberEmail(String email) {
        Member member = memberRepository.findByMemberEmail(email).orElseThrow(() -> new IllegalArgumentException("잘못된 email 입니다."));

        return member.getMyTemperature();
    }
}