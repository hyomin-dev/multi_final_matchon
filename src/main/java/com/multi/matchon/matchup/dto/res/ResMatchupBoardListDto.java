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

    private Long writerId;

    private String writerEmail;

    private String writerName;

    private String teamName;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private Integer currentParticipantCount;

    private Integer maxParticipants;

    private Double minMannerTemperature;

    private Long roomId;

    public ResMatchupBoardListDto(Long boardId, Long writerId, String writerEmail, String writerName, String teamName, SportsTypeName sportsTypeName, String sportsFacilityName, String sportsFacilityAddress, LocalDateTime matchDatetime, LocalTime matchDuration, Integer currentParticipantCount, Integer maxParticipants, Double minMannerTemperature, Long roomId) {
        this.boardId = boardId;
        this.writerId = writerId;
        this.currentParticipantCount = currentParticipantCount;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.maxParticipants = maxParticipants;
        this.minMannerTemperature = minMannerTemperature;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsFacilityAddress = sportsFacilityAddress;
        this.sportsTypeName = sportsTypeName;
        this.teamName = teamName;
        this.writerEmail = writerEmail;
        this.writerName = writerName;
        this.roomId = roomId;
    }
}
