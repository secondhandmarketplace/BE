package com.kdt.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PriceRecommendationResponse {
    private BigDecimal recommendedMinPrice;
    private BigDecimal recommendedMaxPrice;
    private BigDecimal recommendedAveragePrice;
    private List<String> priceFactors;
    private List<PriceImpact> priceImpacts;
    private String priceAdjustmentSuggestion;
    private String marketStrategy;
    private String originalResponse;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime timestamp;
    private String sortedBy;
    private String dataSource;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceImpact {
        private String factorName;
        private BigDecimal impact;
        private String description;
    }

    public PriceRecommendationResponse() {
        this.success = true;
        this.timestamp = LocalDateTime.now();
        this.sortedBy = "latest";
        this.dataSource = "INTERNAL_DB_ONLY";
    }
}
