package ac.su.kdt.secondhandmarketplace.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter

// 상품 추천 응답 DTO
// 이 DTO는 상품 추천 결과를 클라이언트에 전달하기 위해 사용.
@RequiredArgsConstructor
public class ProductRecommendationResponse {
    private Long productId;           // 추천 상품 ID
    private String recommendationReason;  // 추천 이유

    public ProductRecommendationResponse(Long productId, String reason) {
        this.productId = productId;
        this.recommendationReason = reason;
    }
} 