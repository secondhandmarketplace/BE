package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO chatmessageDTO) {
        chatMessageService.sendMessage(chatmessageDTO);
        return chatmessageDTO; // 이 반환값이 구독자에게 전달됨
    }
}
