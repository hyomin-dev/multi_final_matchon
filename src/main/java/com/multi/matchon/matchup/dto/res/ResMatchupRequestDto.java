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


    private String boardWriterEmail;

    private String boardWriter;

    private String applicantEmail;

    private String applicant;

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

    private String selfIntro;

    public ResMatchupRequestDto(String boardWriterEmail, String boardWriter,String applicantEmail, String applicant, Long boardId, Long requestId , SportsTypeName sportsTypeName, String sportsFacilityName, String sportsFacilityAddress, LocalDateTime matchDatetime, LocalTime matchDuration, Integer currentParticipantCount, Integer maxParticipants, Integer participantCount, Status matchupStatus, String selfIntro) {
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
        this.boardWriterEmail = boardWriterEmail;
        this.applicantEmail = applicantEmail;
        this.currentParticipantCount = currentParticipantCount;
        this.maxParticipants = maxParticipants;
    }
}
