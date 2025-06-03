package com.kdt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomResponseDTO {
    private Long chatroomId;
    private Long itemId;
    private String itemTitle;
    private String buyerId;       // ✅ 추가됨
    private String buyerUsername;
    private String sellerUsername;
    private String sellerId;
    private Long createdAt;
    private String latestMessage;
}
