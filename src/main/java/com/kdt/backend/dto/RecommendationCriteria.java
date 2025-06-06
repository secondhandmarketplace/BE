package com.kdt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationCriteria {
    private String category;
    private Integer minPrice;
    private Integer maxPrice;
    private String condition;
    private String sortBy; // "latest", "price", "popular"
    private Integer limit;

    // 기본값 설정
    public static RecommendationCriteria defaultCriteria() {
        return RecommendationCriteria.builder()
                .sortBy("latest") // 사용자 선호사항: 최근 등록순
                .limit(10)
                .build();
    }
}
