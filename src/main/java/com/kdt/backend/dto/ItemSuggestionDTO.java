package com.kdt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSuggestionDTO {
    private Long itemId;
    private String title;
    private String thumbnail;
    private Integer price;
    private String category;
    private String status;
    private String sellerId;

    // ✅ 호환성을 위한 별칭 메서드들
    public Long getId() {
        return this.itemId;
    }

    public void setId(Long id) {
        this.itemId = id;
    }

    public String getImageUrl() {
        return this.thumbnail;
    }

    public void setImageUrl(String imageUrl) {
        this.thumbnail = imageUrl;
    }
}
