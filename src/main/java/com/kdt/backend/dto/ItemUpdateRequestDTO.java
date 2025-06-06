package com.kdt.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemUpdateRequestDTO {
    private String title;
    private Integer price;
    private String description;
    private String category;
    private String meetLocation;
    private String condition; // 상품 상태
    private List<String> itemImages;
    private String thumbnail;
    private List<String> tags;

    // 삭제할 이미지 ID 목록
    private List<Long> deleteImageIds;

    // 새로 추가할 이미지 URL 목록
    private List<String> newImageUrls;
}
