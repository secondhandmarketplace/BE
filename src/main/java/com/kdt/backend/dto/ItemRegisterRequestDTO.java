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
    private String value; // 상품 상태
    private String description;
    private String category;
    private String imageUrl; // 대표 이미지
    private List<String> itemImages; // 모든 이미지
    private String status;
    private String meetLocation;
    private String sellerId;
}

