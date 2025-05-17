package com.multi.matchon.common.dto.res;

<<<<<<< HEAD

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
=======
import lombok.*;
>>>>>>> bf0050f (기능 1차 완료 - 로그인/회원가입/로그아웃 로직 (CSS 제외) + AuthServiceImpl 수정x)

import lombok.*;



@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
public class SportsTypeDto {
    private Long id;
    private String sportsTypeName;

}
