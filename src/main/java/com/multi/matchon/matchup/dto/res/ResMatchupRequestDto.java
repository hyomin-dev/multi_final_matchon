package com.multi.matchon.matchup.dto.res;

import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.domain.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ResMatchupRequestDto {

    private String boardWriter;

    private String applicant;

    private Long boardId;

    private Long requestId;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer participantCount;

    private Status matchupStatus;

    private String selfIntro;

    public ResMatchupRequestDto(String boardWriter, String applicant, Long boardId, Long requestId , SportsTypeName sportsTypeName, String sportsFacilityName, String sportsFacilityAddress, LocalDateTime matchDatetime, LocalTime matchDuration, Integer participantCount, Status matchupStatus, String selfIntro) {
        this.boardWriter = boardWriter;
        this.applicant = applicant;
        this.boardId = boardId;
        this.requestId = requestId;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsFacilityAddress = sportsFacilityAddress;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.participantCount = participantCount;
        this.matchupStatus = matchupStatus;
        this.selfIntro = selfIntro;
        this.sportsTypeName = sportsTypeName;
    }
}
