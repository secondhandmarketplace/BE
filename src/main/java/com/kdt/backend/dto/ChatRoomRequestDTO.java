package com.kdt.backend.dto;

import lombok.Data;

@Data
public class ChatRoomRequestDTO {
    private Long itemTransactionId;
    private String buyerId;
    private String sellerId;
}
