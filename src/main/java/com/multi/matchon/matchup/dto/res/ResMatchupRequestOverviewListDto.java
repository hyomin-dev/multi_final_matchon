package com.multi.matchon.matchup.dto.res;


import com.multi.matchon.common.domain.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResMatchupRequestOverviewListDto {

    private String applicantName;

    private Long requestId;

    private Integer participantCount;

    private Status matchupStatus;

    private Integer matchupRequestSubmittedCount;

    private Integer matchupCancelSubmittedCount;

    private Boolean isDeleted;

    public ResMatchupRequestOverviewListDto(
            String applicantName,
            Long requestId,
            Integer participantCount,
            Status matchupStatus,
            Integer matchupRequestSubmittedCount,
            Integer matchupCancelSubmittedCount,
            Boolean isDeleted
            ) {
        this.isDeleted = isDeleted;
        this.matchupCancelSubmittedCount = matchupCancelSubmittedCount;
        this.matchupRequestSubmittedCount = matchupRequestSubmittedCount;
        this.matchupStatus = matchupStatus;
        this.participantCount = participantCount;
        this.requestId = requestId;
        this.applicantName = applicantName;
    }
}
