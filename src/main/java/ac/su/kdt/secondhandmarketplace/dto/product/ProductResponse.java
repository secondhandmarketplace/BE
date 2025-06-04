package ac.su.kdt.secondhandmarketplace.dto.product;

import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId; // 상품 고유 식별자
    private Long categoryId; // 상품 카테고리 ID
    private String categoryName; // 상품 카테고리명
    private Long userId; // 판매자 ID
    private String username; // 판매자 이름
    private Long aiPredictedCategoryId; // AI가 예측한 카테고리 ID
    private String aiPredictedCategoryName; // AI가 예측한 카테고리명
    private String title; // 상품명
    private String description; // 상품 상세 설명
    private BigDecimal price; // 상품 가격
    private String status; // 상품 상태
    private BigDecimal aiPriceMin; // AI 예측 최소 가격
    private BigDecimal aiPriceMax; // AI 예측 최대 가격
    private Integer viewCount; // 조회수
    private Integer chatCount; // 채팅 수
    private String locationInfo; // 위치 정보
    private LocalDateTime createAt; // 상품 등록 시간
    private LocalDateTime updateAt; // 상품 정보 수정 시간
    private LocalDateTime refreshedAt; // 상품 새로고침 시간
    private LocalDateTime soldAt; // 판매 완료 시간
    private List<ProductImageResponse> images; // 상품 이미지 목록

    // ProductResponse를 생성하는 정적 팩토리 메서드
    public static ProductResponse fromEntity(ac.su.kdt.secondhandmarketplace.entity.Product product) {
        // Product 엔티티를 ProductResponse DTO로 변환합니다.
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getId()); // 상품 ID 설정
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null); // 카테고리 ID 설정
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null); // 카테고리명 설정
        response.setUserId(product.getUser() != null ? product.getUser().getId() : null); // 판매자 ID 설정
        response.setUsername(product.getUser() != null ? product.getUser().getUsername() : null); // 판매자 이름 설정
        response.setAiPredictedCategoryId(product.getAiPredictedCategory() != null ? product.getAiPredictedCategory().getId() : null); // AI 예측 카테고리 ID 설정
        response.setAiPredictedCategoryName(product.getAiPredictedCategory() != null ? product.getAiPredictedCategory().getCategoryName() : null); // AI 예측 카테고리명 설정
        response.setTitle(product.getTitle()); // 상품명 설정
        response.setDescription(product.getDescription()); // 상세 설명 설정
        response.setPrice(product.getPrice()); // 가격 설정
        response.setStatus(product.getStatus() != null ? product.getStatus().getKoreanStatus() : null); // 상태 설정
        response.setAiPriceMin(product.getAiPriceMin()); // AI 예측 최소 가격 설정
        response.setAiPriceMax(product.getAiPriceMax()); // AI 예측 최대 가격 설정
        response.setViewCount(product.getViewCount()); // 조회수 설정
        response.setChatCount(product.getChatCount()); // 채팅 수 설정
        response.setLocationInfo(product.getLocationInfo()); // 위치 정보 설정
        response.setCreateAt(product.getCreateAt()); // 생성 시간 설정
        response.setUpdateAt(product.getUpdateAt()); // 업데이트 시간 설정
        response.setRefreshedAt(product.getRefreshedAt()); // 새로고침 시간 설정
        response.setSoldAt(product.getSoldAt()); // 판매 완료 시간 설정
        // 상품 이미지 리스트를 ProductImageResponse DTO 리스트로 변환하여 설정합니다.
        response.setImages(product.getImages().stream()
                .map(ProductImageResponse::fromEntity) // 각 ProductImage 엔티티를 ProductImageResponse DTO로 매핑합니다.
                .toList()); // 결과를 리스트로 수집합니다.
        return response; // 변환된 ProductResponse DTO를 반환합니다.
    }
}