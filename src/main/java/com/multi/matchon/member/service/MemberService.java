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

    public String searchByEmail(String email) {
        Member member = memberRepository.findByEmailWithTeam(email).orElseThrow(() -> new IllegalArgumentException("잘못된 종목 ID입니다."));

        return member.getTeam().getTeamName();
    }
}
