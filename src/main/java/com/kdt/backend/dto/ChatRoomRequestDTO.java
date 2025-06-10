package com.kdt.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ChatRoomRequestDTO {
    private Long itemTransactionId; // 거래 아이템 ID
    private String buyerId; // 구매자 ID
    private String sellerId; // 판매자 ID
}
