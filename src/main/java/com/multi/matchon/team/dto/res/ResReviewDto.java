package com.multi.matchon.team.dto.res;

import com.multi.matchon.team.domain.Response;
import com.multi.matchon.team.domain.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResReviewDto {
    private Long id; // ✅ THIS is required!
    private String reviewerName;
    private int rating;
    private String content;
    private LocalDateTime createdDate;
    private String response;
    private LocalDateTime respondedAt;

    public static ResReviewDto from(Review review, Response response) {
        return ResReviewDto.builder()
                .id(review.getId()) // ✅ THIS too!
                .reviewerName(review.getMember().getMemberName())
                .rating(review.getReviewRating())
                .content(review.getContent())
                .createdDate(review.getCreatedDate())
                .response(response != null ? response.getReviewResponse() : null)
                .respondedAt(response != null ? response.getCreatedDate() : null)
                .build();
    }
}