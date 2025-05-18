package com.multi.matchon.member.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

//    @NotNull(message = "종목 ID는 필수입니다.")
//    private Long sportsTypeId;

//    @NotNull(message = "포지션 ID는 필수입니다.")
//    private Long positionId;
}
