package ac.su.kdt.secondhandmarketplace.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private Long imageId; // 이미지 고유 식별자
    private String imageUrl; // 이미지 URL
    private Integer sequence; // 이미지 순서

    // ProductImageResponse를 생성하는 정적 팩토리 메서드
    public static ProductImageResponse fromEntity(ac.su.kdt.secondhandmarketplace.entity.ProductImage image) {
        // ProductImage 엔티티를 ProductImageResponse DTO로 변환
        return new ProductImageResponse(image.getId(), image.getImageUrl(), image.getSequence()); // 새로운 ProductImageResponse 객체를 생성하여 반환합니다.
    }
}