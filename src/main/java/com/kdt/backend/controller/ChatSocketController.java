package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.dto.UserDTO;
import com.kdt.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ WebSocket 메시지 수신 및 브로드캐스트
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload ChatMessageDTO message,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails → senderId 변환
        String senderId = userDetails.getUsername();

        // 두 번째 인자로 senderId 전달
        ChatMessageDTO savedMessage = chatMessageService.sendMessage(message, senderId);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId(),
                savedMessage
        );
    }
}
