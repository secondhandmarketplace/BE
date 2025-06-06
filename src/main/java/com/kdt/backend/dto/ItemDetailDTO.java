package com.kdt.backend.dto;

import com.kdt.backend.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDetailDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String status;
    private String condition;
    private List<String> itemImages;
    private String sellerName;
    private String sellerId;
    private String sellerProfileImage;
    private LocalDateTime regDate;
    private Integer viewCount;
    private String meetLocation;
    private List<String> tags;
    private List<ItemResponseDTO> relatedItems;
    private Double averageRating;
    private Integer reviewCount;

    public static ItemDetailDTO from(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDetailDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .description(item.getDescription())
                .price(BigDecimal.valueOf(item.getPrice()))
                .category(item.getCategory())
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .condition(item.getItemCondition() != null ? item.getItemCondition() : "상태 미표시")
                .itemImages(item.getItemImages() != null ?
                        item.getItemImages().stream()
                                .map(img -> img.getPhotoPath())
                                .toList() : List.of())
                .sellerName(item.getSeller() != null ? item.getSeller().getName() : "알 수 없음")
                .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                .regDate(item.getRegDate())
                .viewCount(item.getViewCount())
                .meetLocation(item.getMeetLocation())
                .tags(List.of()) // 기본값
                .build();
    }
}
