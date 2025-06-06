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
public class ItemListResponseDTO {
    private List<ItemResponseDTO> items;
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
    private String sortBy;
    private Boolean hasNext;
    private Boolean hasPrevious;

    // 편의 메서드들
    public Boolean getHasNext() {
        return this.currentPage != null && this.totalPages != null &&
                this.currentPage < this.totalPages - 1;
    }

    public Boolean getHasPrevious() {
        return this.currentPage != null && this.currentPage > 0;
    }
}
