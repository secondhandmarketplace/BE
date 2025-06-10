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
    private String nickname; // 채팅방 생성자의 이름
    private String otherUserName; // 상대방 사용자 이름
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
    private String sellerId; // ✅ 판매자 아이디 필드 추가
}
