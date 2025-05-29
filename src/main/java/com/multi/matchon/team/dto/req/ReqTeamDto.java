package com.multi.matchon.team.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqTeamDto {
    private String teamName;

    private String teamRegion;

    private Boolean recruitmentStatus;

    private String teamIntroduction;

    private MultipartFile teamImageFile;

    private List<String> recruitingPositions;

    private Double teamRatingAverage;

    private Long teamId;

}
