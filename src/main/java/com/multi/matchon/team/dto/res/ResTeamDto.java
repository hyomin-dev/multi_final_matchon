package com.multi.matchon.team.dto.res;


import com.multi.matchon.team.domain.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResTeamDto {

    private Long team_id;

    private String teamName;

    private String teamRegion;

    private Boolean RecruitmentStatus;


    private String teamIntroduction;

    private MultipartFile teamImageFile;

    private List<String> recruitingPositions;

    private Double teamRatingAverage;


    public static ResTeamDto from(Team team) {
        return ResTeamDto.builder()
                .teamName(team.getTeamName())
                .teamRegion(team.getTeamRegion().name())
                .teamRatingAverage(team.getTeamRatingAverage())
                .RecruitmentStatus(team.getRecruitmentStatus())
                .recruitingPositions(
                        team.getRecruitingPositions().stream()
                                .map(rp -> rp.getPositions().getPositionName().name())
                                .collect(Collectors.toList())
                )
                .build();
    }

}


