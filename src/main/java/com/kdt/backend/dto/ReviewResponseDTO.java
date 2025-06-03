// ReviewResponseDTO.java
package com.kdt.backend.dto;

import com.kdt.backend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ReviewResponseDTO {
    private int rating;
    private String comment;
    private String reviewerNickname;
    private LocalDateTime createdAt;
    private String itemTitle; // ← 추가

    public static ReviewResponseDTO fromEntity(Review review) {
        return ReviewResponseDTO.builder()
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewerNickname(review.getReviewer().getName())
                .itemTitle(review.getItem().getTitle()) // ← 추가
                .createdAt(review.getCreatedAt())
                .build();
    }
}
