package ac.su.kdt.secondhandmarketplace.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequest {
    private String imageUrl; // 이미지 URL

    private Integer sequence; // 이미지 순서
}