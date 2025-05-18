package com.multi.matchon.matchup.dto.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqMatchupBoardDto {

    private String sportsType;

    private String teamName;

    private String teamIntro;

    private MultipartFile reservationFile;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDate;

    private Integer currentParticipants;

    private Integer maxParticipants;

    private Double minMannerTemperature;

    private String matchIntro;




}
