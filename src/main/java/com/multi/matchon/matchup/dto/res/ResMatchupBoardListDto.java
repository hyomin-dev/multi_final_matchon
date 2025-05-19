package com.multi.matchon.matchup.dto.res;

import com.multi.matchon.common.domain.SportsTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ResMatchupBoardListDto {

    private Long boardId;

    private String writer;

    private String teamName;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer currentParticipantCount;

    private Integer maxParticipants;

    private Double minMannerTemperature;

    public ResMatchupBoardListDto(Long boardId, String writer, String teamName, SportsTypeName sportsTypeName, String sportsFacilityName, LocalDateTime matchDatetime, LocalTime matchDuration, Integer currentParticipantCount, Integer maxParticipants, Double minMannerTemperature ) {
        this.boardId = boardId;
        this.currentParticipantCount = currentParticipantCount;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.maxParticipants = maxParticipants;
        this.minMannerTemperature = minMannerTemperature;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsTypeName = sportsTypeName;
        this.teamName = teamName;
        this.writer = writer;
    }
}
