package com.multi.matchon.matchup.dto.res;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResMatchupRatingListDto {

    private Long boardId;

    private String evalName;

    private String targetName;

    private Long sendedEvalId;

    private Long sendedTargetId;

    private Boolean isCompletedSend;

    private Long receivedEvalId;

    private Long receivedTargetId;

    private Boolean isCompletedReceive;


    public ResMatchupRatingListDto(Long boardId, String evalName, String targetName,
                                   Long sendedEvalId, Long sendedTargetId, Boolean isCompletedSend,
                                   Long receivedEvalId, Long receivedTargetId, Boolean isCompletedReceive) {
        this.boardId = boardId;
        this.evalName = evalName;
        this.isCompletedReceive = isCompletedReceive;
        this.isCompletedSend = isCompletedSend;
        this.receivedEvalId = receivedEvalId;
        this.receivedTargetId = receivedTargetId;
        this.sendedEvalId = sendedEvalId;
        this.sendedTargetId = sendedTargetId;
        this.targetName = targetName;
    }
}
