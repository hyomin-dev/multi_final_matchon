package com.multi.matchon.matchup.dto.res;

import com.multi.matchon.common.domain.SportsTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ResMatchupMyGameListDto {

    private Long boardId;

    private LocalDateTime matchDatetime;

    private LocalTime matchDuration;

    private SportsTypeName sportsTypeName;

    private String sportsFacilityName;

    private String sportsFacilityAddress;

    private Boolean isRatingInitialized;



    public ResMatchupMyGameListDto(Long boardId, LocalDateTime matchDatetime, LocalTime matchDuration, SportsTypeName sportsTypeName, String sportsFacilityName, String sportsFacilityAddress, Boolean isRatingInitialized) {
        this.boardId = boardId;
        this.matchDatetime = matchDatetime;
        this.matchDuration = matchDuration;
        this.sportsFacilityAddress = sportsFacilityAddress;
        this.sportsFacilityName = sportsFacilityName;
        this.sportsTypeName = sportsTypeName;
        this.isRatingInitialized = isRatingInitialized;
    }
}
