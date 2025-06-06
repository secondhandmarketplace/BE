package com.kdt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private String content;
    private List<ItemResponseDTO> recommendedItems;
    private String explanation;
    private String requestType;
    private Integer totalCount;
    private LocalDateTime timestamp;
    private String aiExplanation;

    // 가격 통계 정보
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // 추천 성공 여부
    private Boolean success;
    private String errorMessage;

    // 정렬 정보 (사용자 선호사항: 최근 등록순)
    private String sortedBy;

    // 기본 생성자에서 초기값 설정
    public RecommendationResponse() {
        this.recommendedItems = new ArrayList<>();
        this.success = true;
        this.timestamp = LocalDateTime.now();
        this.sortedBy = "latest"; // 기본값: 최근 등록순
        this.totalCount = 0;
    }

    // 간단한 응답을 위한 생성자
    public RecommendationResponse(String content) {
        this();
        this.content = content;
    }

    // 에러 응답을 위한 생성자
    public RecommendationResponse(String content, String errorMessage) {
        this();
        this.content = content;
        this.errorMessage = errorMessage;
        this.success = false;
    }
}
