package com.kdt.backend.dto;

import com.kdt.backend.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String status;
    private String condition;
    private String imageUrl;
    private List<String> itemImages;
    private String sellerName;
    private String sellerId;
    private LocalDateTime regDate;
    private Integer viewCount;
    private Double averageRating;
    private Integer reviewCount;
    private String meetLocation;
    private String thumbnail;
    private List<String> tags;
    private String value; // 상품 상태

    public static ItemResponseDTO from(Item item) {
        if (item == null) {
            return null;
        }

        return ItemResponseDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .description(item.getDescription())
                .price(BigDecimal.valueOf(item.getPrice()))
                .category(item.getCategory())
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .condition(getConditionValue(item))
                .value(getConditionValue(item)) // value 필드도 설정
                .imageUrl(getFirstImageUrl(item))
                .itemImages(getImageUrls(item))
                .sellerName(item.getSeller() != null ? item.getSeller().getName() : "알 수 없음")
                .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                .regDate(item.getRegDate())
                .viewCount(item.getViewCount())
                .meetLocation(item.getMeetLocation())
                .thumbnail(item.getThumbnail())
                .tags(item.getTags())
                .build();
    }

    /**
     * Item 엔티티의 실제 필드명에 맞춰 상태 값 추출
     */
    private static String getConditionValue(Item item) {
        if (item.getItemCondition() != null) {
            return item.getItemCondition();
        }
        return "상태 미표시";
    }

    private static String getFirstImageUrl(Item item) {
        if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
            return item.getItemImages().get(0).getPhotoPath();
        }
        if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
            return item.getThumbnail();
        }
        return "/assets/default-image.png";
    }

    private static List<String> getImageUrls(Item item) {
        if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
            return item.getItemImages().stream()
                    .map(itemImage -> itemImage.getPhotoPath())
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
