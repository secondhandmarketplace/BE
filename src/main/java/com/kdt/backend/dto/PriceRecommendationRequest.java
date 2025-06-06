package com.kdt.backend.dto;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRecommendationRequest {
    private String userRequest;

    @Nullable
    private Long productId;

    private String category;
    private String condition;

    @Nullable
    private BigDecimal currentPrice;

    private LocalDateTime requestTime;

    @Nullable
    private String userId;

    private String productName;

    @Nullable
    private String usagePeriod;

    public PriceRecommendationRequest(String userRequest) {
        this.userRequest = userRequest;
        this.requestTime = LocalDateTime.now();
    }
}
