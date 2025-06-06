package com.kdt.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSearchDTO {
    private String keyword;
    private String category;
    private Integer minPrice;
    private Integer maxPrice;
    private String condition; // 상품 상태
    private String status; // 판매 상태
    private String sortBy; // 정렬 기준
    private Integer page;
    private Integer size;

    // 기본값 설정
    public String getSortBy() {
        return this.sortBy != null ? this.sortBy : "regDate"; // 기본: 등록일순
    }

    public Integer getPage() {
        return this.page != null ? this.page : 0;
    }

    public Integer getSize() {
        return this.size != null ? this.size : 20;
    }
}
