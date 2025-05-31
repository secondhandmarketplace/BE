package ac.su.kdt.secondhandmarketplace.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 가격 추천 응답 DTO
 * AI가 제안한 가격 추천 정보를 담고 있습니다.
 */
@Data
public class PriceRecommendationResponse {
    /**
     * 추천 최소 가격
     */
    private BigDecimal recommendedMinPrice;

    /**
     * 추천 최대 가격
     */
    private BigDecimal recommendedMaxPrice;

    /**
     * 추천 평균 가격
     */
    private BigDecimal recommendedAveragePrice;

    /**
     * 가격 추천의 주요 근거
     */
    private List<String> priceFactors;

    /**
     * 각 가격 결정 요소가 최종 가격에 미친 영향
     */
    private List<PriceImpact> priceImpacts;

    /**
     * 가격 조정 제안 (현재 가격이 있는 경우)
     */
    private String priceAdjustmentSuggestion;

    /**
     * 시장 상황에 따른 가격 전략 제안
     */
    private String marketStrategy;

    /**
     * AI의 원본 응답 메시지
     */
    private String originalResponse;

    /**
     * 가격 결정 요소의 영향도를 나타내는 내부 클래스
     */
    @Data
    public static class PriceImpact {
        /**
         * 가격 결정 요소 이름
         */
        private String factorName;

        /**
         * 영향도 (0.0 ~ 1.0)
         */
        private BigDecimal impact;

        /**
         * 영향 설명
         */
        private String description;
    }
} 