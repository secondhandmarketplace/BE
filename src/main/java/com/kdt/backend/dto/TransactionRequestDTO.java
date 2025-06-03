package com.kdt.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequestDTO {
    private String buyerId;
    private String sellerId;
    private Long itemId;
}
