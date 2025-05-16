package com.multi.matchon.common.auth.service;

import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsType;
import com.multi.matchon.common.jwt.domain.RefreshToken;
import com.multi.matchon.common.jwt.repository.RefreshTokenRepository;
import com.multi.matchon.common.jwt.service.JwtTokenProvider;
import com.multi.matchon.common.repository.PositionsRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import com.multi.matchon.member.dto.req.HostSignupRequestDto;
import com.multi.matchon.member.dto.req.LoginRequestDto;
import com.multi.matchon.member.dto.req.UserSignupRequestDto;
import com.multi.matchon.member.dto.res.TokenResponseDto;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public void signupUser(UserSignupRequestDto dto) {
        if (memberRepository.findByMemberEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (dto.getSportsTypeId() == null) {
            throw new IllegalArgumentException("종목 ID는 필수입니다.");
        }

        if (dto.getPositionId() == null) {
            throw new IllegalArgumentException("포지션 ID는 필수입니다.");
        }

        SportsType sportstype = sportsTypeRepository.findById(dto.getSportsTypeId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 종목 ID입니다."));

        Positions position = positionsRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 포지션 ID입니다."));

        Member member = Member.builder()
                .memberEmail(dto.getEmail())
                .memberPassword(passwordEncoder.encode(dto.getPassword()))
                .memberName(dto.getName())
                .memberRole(MemberRole.user)
                .sportsType(sportstype)
                .positions(position)
                .myTemperature(36.5)
                .pictureAttachmentEnabled(true)
                .isDeleted(false)
                .build();

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void signupHost(HostSignupRequestDto dto) {
        if (memberRepository.findByMemberEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (dto.getSportsTypeId() == null) {
            throw new IllegalArgumentException("종목 ID는 필수입니다.");
        }


        SportsType sportsType = sportsTypeRepository.findById(dto.getSportsTypeId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 종목 ID입니다."));

        Member host = Member.builder()
                .memberEmail(dto.getEmail())
                .memberPassword(passwordEncoder.encode(dto.getPassword()))
                .memberName(dto.getName())
                .memberRole(MemberRole.host)
                .sportsType(sportsType)
                .pictureAttachmentEnabled(true)
                .isDeleted(false)
                .build();

        memberRepository.save(host);
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

        RefreshToken token = RefreshToken.builder()
                .member(member)
                .refreshTokenData(refreshToken)
                .refreshTokenExpiredDate(LocalDateTime.now().plusDays(14))
                .build();

        refreshTokenRepository.save(token);

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

        RefreshToken saved = refreshTokenRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new RuntimeException("저장된 RefreshToken 없음"));

        if (!saved.getRefreshTokenData().equals(refreshToken)) {
            throw new RuntimeException("RefreshToken 불일치");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email, member.getMemberRole());
        return new TokenResponseDto(newAccessToken, refreshToken);
    }
}
