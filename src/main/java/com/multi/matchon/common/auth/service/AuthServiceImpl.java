package com.multi.matchon.common.auth.service;

import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.jwt.domain.RefreshToken;
import com.multi.matchon.common.jwt.repository.RefreshTokenRepository;
import com.multi.matchon.common.jwt.service.JwtTokenProvider;
import com.multi.matchon.common.repository.PositionsRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import com.multi.matchon.member.dto.req.LoginRequestDto;
import com.multi.matchon.member.dto.req.SignupRequestDto;
import com.multi.matchon.member.dto.res.TokenResponseDto;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final MemberRepository memberRepository;
    private final SportsTypeRepository sportsTypeRepository;
    private final PositionsRepository positionsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void signupUser(SignupRequestDto dto) {
        if (memberRepository.findByMemberEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 항상 SOCCER 종목으로 설정
        SportsType soccer = sportsTypeRepository.findBySportsTypeName(SportsTypeName.SOCCER)
                .orElseThrow(() -> new IllegalArgumentException("SOCCER 종목이 존재하지 않습니다."));


        Member member = Member.builder()
                .memberEmail(dto.getEmail())
                .memberPassword(passwordEncoder.encode(dto.getPassword()))
                .memberName(dto.getName())
                .memberRole(MemberRole.USER)
                .sportsType(soccer)
                //.positions(position)
                .myTemperature(36.5)
                .pictureAttachmentEnabled(true)
                .isDeleted(false)
                .build();

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void signupHost(SignupRequestDto dto) {
        if (memberRepository.findByMemberEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 항상 SOCCER 종목으로 설정
        SportsType soccer = sportsTypeRepository.findBySportsTypeName(SportsTypeName.SOCCER)
                .orElseThrow(() -> new IllegalArgumentException("SOCCER 종목이 존재하지 않습니다."));


        Member member = Member.builder()
                .memberEmail(dto.getEmail())
                .memberPassword(passwordEncoder.encode(dto.getPassword()))
                .memberName(dto.getName())
                .memberRole(MemberRole.HOST)
                .sportsType(soccer)
                .pictureAttachmentEnabled(true)
                .isDeleted(false)
                .build();

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void logout(String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        refreshTokenRepository.deleteByMember(member);
    }


    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto dto) {
        Member member = memberRepository.findByMemberEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getMemberPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberEmail(), member.getMemberRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberEmail());

        // 기존 RefreshToken 존재 여부 확인
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByMember(member);

        if (existingToken.isPresent()) {
            // 기존 토큰 갱신 (update)
            RefreshToken token = existingToken.get();
            token.update(refreshToken, LocalDateTime.now().plusDays(14));
        } else {
            // 새로 저장 (insert)
            RefreshToken token = RefreshToken.builder()
                    .member(member)
                    .refreshTokenData(refreshToken)
                    .refreshTokenExpiredDate(LocalDateTime.now().plusDays(14))
                    .build();
            refreshTokenRepository.save(token);
        }

        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenResponseDto reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 RefreshToken입니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        RefreshToken saved = refreshTokenRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("저장된 RefreshToken 없음"));

        if (!saved.getRefreshTokenData().equals(refreshToken)) {
            throw new RuntimeException("RefreshToken 불일치");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email, member.getMemberRole());
        return new TokenResponseDto(newAccessToken, refreshToken);
    }
}
