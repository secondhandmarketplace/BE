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
public class ChatMessageDTO {
    private Long messageId;
    private Long chatRoomId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;

    public static ChatMessageDTOBuilder messageBuilder(Long chatRoomId, String senderId, String content) {
        return ChatMessageDTO.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false);
    }
}
