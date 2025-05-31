package com.multi.matchon.member.service;

import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    // 마이페이지용
    public Member findForMypage(String email) {
        return memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }


    /*
    * 팀 이름 조회용
    * */
    @Transactional(readOnly = true)
    public String findTeamNameByMember(Member loginMember) {
        return memberRepository.findTeamNameByMember(loginMember).orElseThrow(() -> new IllegalArgumentException("현재 소속팀이 없습니다."));
    }

    // 마이온도 조회용
    @Transactional(readOnly = true)
    public Double findMyTemperatureByMember(Member loginMember) {
        return memberRepository.findMyTemperatureByMember(loginMember).orElseThrow(() -> new IllegalArgumentException("회원의 매너온도가 설정되어 있지 않습니다."));

    }

    //이메일로 회원조회
    public Member findByEmail(String email) {
        return memberRepository.findByMemberEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

}