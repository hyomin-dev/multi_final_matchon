package com.multi.matchon.team.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResTeamDto {

    private Long team_id;

    private String teamName;

    private String teamRegion;

    private String recruitmentStatus;

    private String teamIntroduction;

    private MultipartFile teamImageFile;

    private List<String> recruitingPositions;

    private Double teamRatingAverage;

}
