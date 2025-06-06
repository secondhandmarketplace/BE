package com.kdt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDTO {
    private Long id; // ✅ Long 타입 유지
    private Long roomId; // ✅ Long 타입 유지
    private String nickname;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastTimestamp;
    private LocalDateTime updatedAt;
    private String itemImageUrl;
    private String imageUrl;
    private Long itemId;
    private String itemTitle;
    private Integer itemPrice;
    private Integer unreadCount;
    private String otherUserId;
    private String status;

    // ✅ 레거시 생성자 (호환성 유지)
    public ChatRoomResponseDTO(Long chatroomId, Long itemId, String itemTitle,
                               String buyerId, String buyerUsername, String sellerUsername,
                               String sellerId, Long createdAt, String latestMessage) {
        this.id = chatroomId;
        this.roomId = chatroomId;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.otherUserId = sellerId;
        this.nickname = sellerUsername;
        this.otherUserName = sellerUsername;
        this.lastMessage = latestMessage;
        this.status = "active";
        this.unreadCount = 0;
    }
}
