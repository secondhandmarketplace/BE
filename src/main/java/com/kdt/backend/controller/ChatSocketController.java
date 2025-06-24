package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload ChatMessageDTO message,
            @AuthenticationPrincipal Principal principal) {

        try {
            log.info("WebSocket 메시지 수신: roomId={}, senderId={}, content={}",
                message.getChatRoomId(), message.getSenderId(), message.getContent());

            // ✅ 사용자 ID 추출 (메시지의 senderId 우선, Principal 백업)
            String authenticatedUserId = message.getSenderId();
            if (authenticatedUserId == null || authenticatedUserId.trim().isEmpty()) {
                authenticatedUserId = principal != null ? principal.getName() : null;
            }

            if (authenticatedUserId == null || authenticatedUserId.trim().isEmpty() || authenticatedUserId.startsWith("anonymous-")) {
                log.warn("유효하지 않은 사용자 ID: principal={}, senderId={}", principal, message.getSenderId());
                return;
            }

            // ✅ 메시지에 senderId 설정
            message.setSenderId(authenticatedUserId);

            // ✅ 메시지 전송 및 브로드캐스트
            ChatMessageDTO savedMessage = chatMessageService.sendMessage(message, authenticatedUserId);
            messagingTemplate.convertAndSend("/topic/chat/" + message.getChatRoomId(), savedMessage);

            log.info("WebSocket 메시지 전송 완료: messageId={}, roomId={}, senderId={}",
                savedMessage.getMessageId(), message.getChatRoomId(), authenticatedUserId);

        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패: {}", e.getMessage(), e);
            // 에러 메시지를 클라이언트에게 전송
            try {
                ChatMessageDTO errorMessage = ChatMessageDTO.builder()
                    .content("메시지 전송에 실패했습니다: " + e.getMessage())
                    .chatRoomId(message.getChatRoomId())
                    .build();
                messagingTemplate.convertAndSend("/topic/chat/" + message.getChatRoomId(), errorMessage);
            } catch (Exception ex) {
                log.error("에러 메시지 전송 실패: {}", ex.getMessage());
            }
        }
    }
}
