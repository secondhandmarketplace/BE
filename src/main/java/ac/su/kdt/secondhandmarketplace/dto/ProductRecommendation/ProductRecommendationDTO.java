package ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation;

import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

// 상품을 추천하기 위한 DTO 클래스
@Getter
@Setter
public class ProductRecommendationDTO {
    private Long id;                    // 상품 ID
    private String title;               // 상품명
    private BigDecimal price;           // 가격
    private ProductStatus status;              // 상태
    private String category;            // 카테고리
    private String locationInfo;        // 위치 정보
    private Integer viewCount;          // 조회수
    private BigDecimal aiPriceMin;      // AI 예측 최소 가격
    private BigDecimal aiPriceMax;      // AI 예측 최대 가격
    private Double mannerScore;         // 판매자 매너 점수
    private Double averageRating;       // 평균 리뷰 평점
    private String description;         // 상품 설명
    private String sellerName;          // 판매자 이름
    private Integer sellerReviewCount;  // 판매자 리뷰 수

    public static ProductRecommendationDTO fromEntity(Product product, ReviewRepository reviewRepository) {
        ProductRecommendationDTO dto = new ProductRecommendationDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus());
        dto.setCategory(product.getCategory().getCategoryName());
        dto.setLocationInfo(product.getLocationInfo());
        dto.setViewCount(product.getViewCount());
        dto.setAiPriceMin(product.getAiPriceMin());
        dto.setAiPriceMax(product.getAiPriceMax());
        dto.setDescription(product.getDescription());
        
        // 판매자 정보
        if (product.getUser() != null) {
            dto.setMannerScore(product.getUser().getMannerScore().doubleValue());
            dto.setSellerName(product.getUser().getNickname());
            dto.setSellerReviewCount((int) reviewRepository.countByReviewer(product.getUser()));
        }
        
        // 상품 평균 평점
        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        
        return dto;
    }
} 