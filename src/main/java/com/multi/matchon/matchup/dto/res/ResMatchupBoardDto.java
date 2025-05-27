package com.multi.matchon.matchup.dto.res;


import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.SportsTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResMatchupBoardDto {

    private Long boardId;

    private Long memberId;

    private String memberEmail;

    private String memberName;

    private String teamName;

    private SportsTypeName sportsTypeName;

    private String teamIntro;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer currentParticipantCount;

    private Integer maxParticipants;

    private Double minMannerTemperature;

    private MultipartFile reservationFile;

    private String matchDescription;

    private String originalName;

    private String savedName;

    private String savedPath;

    private Double myMannerTemperature;

}
