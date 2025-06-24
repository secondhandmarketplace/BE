package com.kdt.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRoomResponseDTO {
    private Long id; // ✅ 채팅방의 고유 ID, DTO 빌더에서 id()로 사용

    private String otherUserId;
    private String otherUserName;
//    private String otherUserPicture;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private Integer unreadCount;
    private String status;
    private Long itemTransactionId;
    private Long itemId; // ✅ 상품 ID 추가
    private String itemTitle;
    private Integer itemPrice;
    private String itemImageUrl;
}
