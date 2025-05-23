package com.multi.matchon.matchup.dto.res;

import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.domain.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ResMatchupRequestListDto {

    private Long boardId;

    private Long requestId;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer currentParticipantCount;

    private Integer maxParticipants;

    private Integer participantCount;

    private Status matchupStatus;

    public ResMatchupRequestListDto(Long boardId, Long requestId, SportsTypeName sportsTypeName, String sportsFacilityName, String sportsFacilityAddress,  LocalDateTime matchDatetime, LocalTime matchDuration, Integer currentParticipantCount, Integer maxParticipants ,Integer participantCount, Status matchupStatus) {
        this.boardId = boardId;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.matchupStatus = matchupStatus;
        this.participantCount = participantCount;
        this.requestId = requestId;
        this.sportsFacilityAddress = sportsFacilityAddress;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsTypeName = sportsTypeName;
        this.currentParticipantCount = currentParticipantCount;
        this.maxParticipants = maxParticipants;
    }
}
