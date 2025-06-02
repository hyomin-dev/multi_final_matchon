package com.multi.matchon.team.dto.res;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResJoinRequestDetailDto {
    private Long requestId;
    private String nickname;
    private String position;
    private Double temperature;
    private String preferredTime;
    private String introduction;
    // Optionally: private String profileImageUrl;



}