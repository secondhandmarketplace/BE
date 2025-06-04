package ac.su.kdt.secondhandmarketplace.dto.product;

import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductCreateRequest {
    private Long categoryId; // 상품 카테고리 ID

    private Long userId; // 판매자 ID

    private Long aiPredictedCategoryId; // AI가 예측한 카테고리 ID (필수 아님)

    private String title; // 상품명

    private String description; // 상품 상세 설명 (필수 아님)

    private BigDecimal price; // 상품 가격 (필수 아님)

    private ProductStatus status; // 상품 상태

    private BigDecimal aiPriceMin; // AI 예측 최소 가격 (필수 아님)

    private BigDecimal aiPriceMax; // AI 예측 최대 가격 (필수 아님)

    private String locationInfo; // 위치 정보 (필수 아님)

    private List<ProductImageRequest> imageUrls; // 상품 이미지 URL 목록 (ProductImageRequest DTO 사용)
}