package com.kdt.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRegisterRequestDTO {
    private String title;
    private Integer price;
    private List<String> tags;
    private String value;
    private String description;
    private String category;
    private String imageUrl;    // 대표 이미지 (프론트의 imageUrls[0])
    private List<String> imageUrls; // 모든 이미지
    private String status;
    private String meetLocation;
    private String sellerId;
    private String thumbnail;   // 썸네일 (대표 이미지와 동일)
    private String condition;

    public String getCondition() {
        return this.value != null ? this.value : this.condition;
    }
    public void setCondition(String condition) {
        this.value = condition;
        this.condition = condition;
    }
}
