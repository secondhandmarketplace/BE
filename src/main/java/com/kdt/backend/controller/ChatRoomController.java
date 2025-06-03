package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatRoomRequestDTO;
import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO request) {
        ChatRoomResponseDTO response = chatRoomService.createChatRoom(
                request.getItemTransactionId(),
                request.getBuyerId(),
                request.getSellerId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRoomsByUser(@RequestParam String userId) {
        List<ChatRoomResponseDTO> result = chatRoomService.getChatRoomsByUser(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable Long chatRoomId) {
        ChatRoom room = chatRoomService.getChatRoomById(chatRoomId);

        // 최신 메시지 조회 (필요시)
        String latestMessage = chatRoomService.getLatestMessageByRoomId(chatRoomId);

        return ResponseEntity.ok(new ChatRoomResponseDTO(
                room.getChatroomid(),
                room.getItemTransaction().getItem().getItemid(),
                room.getItemTransaction().getItem().getTitle(),
                room.getBuyer().getUserid(),
                room.getBuyer().getName(),
                room.getSeller().getName(),
                room.getSeller().getUserid(),
                room.getCreatedAt(),
                latestMessage // 최신 메시지 필드
        ));
    }

    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long chatRoomId) {
        chatRoomService.deleteChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }
}
