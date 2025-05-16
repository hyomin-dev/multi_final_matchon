package com.multi.matchon.common.auth.service;

import com.multi.matchon.member.dto.req.HostSignupRequestDto;
import com.multi.matchon.member.dto.req.LoginRequestDto;
import com.multi.matchon.member.dto.req.UserSignupRequestDto;
import com.multi.matchon.member.dto.res.TokenResponseDto;

public interface AuthService {

    void signupUser(UserSignupRequestDto dto);

    void signupHost(HostSignupRequestDto dto);

    TokenResponseDto login(LoginRequestDto dto);

    TokenResponseDto reissue(String refreshToken);
}
