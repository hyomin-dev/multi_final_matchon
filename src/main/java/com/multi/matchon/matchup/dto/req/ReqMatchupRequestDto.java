package com.multi.matchon.matchup.dto.req;

import com.multi.matchon.common.domain.SportsTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ReqMatchupRequestDto {

    private Long boardId;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer currentParticipantCount;

    private Integer maxParticipants;

    private String selfIntro;

    private Integer participantCount;

    public ReqMatchupRequestDto(Long boardId, SportsTypeName sportsTypeName, String sportsFacilityName,  String sportsFacilityAddress, LocalDateTime matchDatetime, LocalTime matchDuration, Integer currentParticipantCount,  Integer maxParticipants){
        this.boardId = boardId;
        this.currentParticipantCount = currentParticipantCount;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.maxParticipants = maxParticipants;
        this.sportsFacilityAddress = sportsFacilityAddress;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsTypeName = sportsTypeName;
    }
}
