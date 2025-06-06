package com.kdt.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequest {
    private String userRequest;
    private String userId;
    private String category;
    private Integer minPrice;
    private Integer maxPrice;
    private String sortBy; // "latest", "price", "popular"
    private Integer limit;
    private LocalDateTime requestTime;

    // 기본값 설정을 위한 생성자
    public RecommendationRequest(String userRequest) {
        this.userRequest = userRequest;
        this.sortBy = "latest"; // 기본값: 최근 등록순
        this.limit = 10;
        this.requestTime = LocalDateTime.now();
    }
}
