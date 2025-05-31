package ac.su.kdt.secondhandmarketplace.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 적정가 추천을 위한 요청 DTO
 * 사용자가 상품의 적정가를 추천받기 위해 필요한 정보를 담고 있습니다.
 */
@Data
public class PriceRecommendationRequest {
    /**
     * 사용자의 자연어 요청
     * 예: "아이폰 13 프로 맥스 256기가 중고 가격 추천해줘"
     */
    private String userRequest;

    /**
     * 상품 ID
     * 기존 상품의 가격을 참고할 때 사용
     * 선택적 필드로, 새로운 상품 등록 시에는 null
     */
    private Long productId;

    /**
     * 상품 카테고리
     * 예: "전자기기", "의류", "가구" 등
     * 유사 상품 검색에 사용
     */
    private String category;

    /**
     * 상품 상태
     * 예: "새상품", "중고", "하자있음" 등
     * 가격 결정에 중요한 요소
     */
    private String condition;

    /**
     * 현재 설정된 가격
     * 선택적 필드로, 기존 상품의 가격 조정 시 사용
     * 새로운 상품 등록 시에는 null
     */
    private BigDecimal currentPrice;
} 