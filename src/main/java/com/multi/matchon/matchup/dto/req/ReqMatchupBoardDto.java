package com.multi.matchon.matchup.dto.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import java.time.LocalTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqMatchupBoardDto {



    private String sportsTypeName;

    private String teamName;

    private String teamIntro;

    private MultipartFile reservationFile;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDateTime;

    private Integer matchDuration;

    private Integer currentParticipantsCount;

    private Integer maxParticipants;

    private Double minMannerTemperature;

    private String matchDescription;




}
