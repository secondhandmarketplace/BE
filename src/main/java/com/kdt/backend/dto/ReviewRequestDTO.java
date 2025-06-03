package com.kdt.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReviewRequestDTO {
    private Long itemId;
    private String buyerId;
    private String revieweeId;
    private Long transactionId; // ← 꼭 Long으로!
    private int rating;
    private String comment;
}
