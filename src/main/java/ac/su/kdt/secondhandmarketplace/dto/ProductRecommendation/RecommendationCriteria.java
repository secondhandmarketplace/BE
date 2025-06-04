package ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecommendationCriteria {
    private String productName; // 상품명
    private String category; // 카테고리
    private BigDecimal minPrice; // 최소 가격
    private BigDecimal maxPrice; // 최대 가격
    private String location; // 위치
    private String sortBy; // 정렬 기준 (예: "price", "rating")
    private String sortDirection; // 정렬 방향 ("asc" 또는 "desc")
    private Double minMannerScore; // 최소 매너 점수
    private Double minRating; // 최소 리뷰 평점
}