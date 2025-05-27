package com.multi.matchon.team.dto.res;

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

    public static ResReviewDto from(Review review) {
        return ResReviewDto.builder()
                .id(review.getId()) // ✅ THIS too!
                .reviewerName(review.getMember().getMemberName())
                .rating(review.getReviewRating())
                .content(review.getContent())
                .createdDate(review.getCreatedDate())
                .build();
    }
}