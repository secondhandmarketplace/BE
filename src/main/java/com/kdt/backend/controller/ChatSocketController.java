package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatmessageDTO) {
        System.out.println("📨 받은 메시지 DTO: " + chatmessageDTO);
        chatMessageService.sendMessage(chatmessageDTO);
    }
}
