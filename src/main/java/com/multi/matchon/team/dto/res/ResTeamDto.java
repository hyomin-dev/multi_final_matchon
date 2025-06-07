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

    private Long teamId;

    private String teamName;

    private String teamRegion;

    private Boolean RecruitmentStatus;


    private String teamIntroduction;

    private String imageUrl;

    private List<String> recruitingPositions;

    private Double teamRatingAverage;

    private String createdBy;


    private String starRatingVisual; // ✅ Add this field

    private Long leaderId;


    public static ResTeamDto from(Team team, String imageUrl, double averageRating) {
        return ResTeamDto.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .teamRegion(team.getTeamRegion().name())
                .teamRatingAverage(averageRating) // ← Inject the calculated average
                .starRatingVisual(generateStarRatingVisual(averageRating))  // ⬅️ Add this
                .RecruitmentStatus(team.getRecruitmentStatus())
                .imageUrl(imageUrl)
                .teamIntroduction(team.getTeamIntroduction())
                .createdBy(team.getCreatedPerson())
                .recruitingPositions(
                        team.getRecruitingPositions().stream()
                                .map(rp -> rp.getPositions().getPositionName().name())
                                .collect(Collectors.toList())
                )
                .build();
    }

    private static String generateStarRatingVisual(double rating) {
        if (rating == 0.0) return "N/A";
        int fullStars = (int) Math.floor(rating);
        boolean halfStar = rating - fullStars >= 0.5;
        int emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
        return "⭐".repeat(fullStars) + (halfStar ? "✩" : "") + "☆".repeat(emptyStars);
    }


}


