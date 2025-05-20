package com.multi.matchon.team.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqTeamDto {
    private String teamName;

    private String teamRegion;

    private String recruitmentStatus;

    private String teamIntroduction;

    private MultipartFile teamImageFile;

    private String positionName;

    private Double teamRatingAverage;

}
