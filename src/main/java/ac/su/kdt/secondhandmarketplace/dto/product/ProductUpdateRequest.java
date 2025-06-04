package ac.su.kdt.secondhandmarketplace.dto.product;

import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductUpdateRequest {
    // 상품명 (수정 시 빈 문자열 허용, 업데이트 시 필수가 아님)
    private String title;

    private String description; // 상품 상세 설명 (수정 시 필수 아님)

    private BigDecimal price; // 상품 가격 (수정 시 필수 아님)

    private ProductStatus status; // 상품 상태

    private BigDecimal aiPriceMin; // AI 예측 최소 가격 (수정 시 필수 아님)

    private BigDecimal aiPriceMax; // AI 예측 최대 가격 (수정 시 필수 아님)

    private String locationInfo; // 위치 정보 (수정 시 필수 아님)

    private List<ProductImageRequest> imageUrls; // 상품 이미지 URL 목록 (수정 시 기존 이미지 제거 및 추가를 위해 사용)
}