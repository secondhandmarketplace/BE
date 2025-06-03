package com.kdt.backend.service;

import com.kdt.backend.dto.MessageDTO;
import com.kdt.backend.dto.MessageResponseDTO;
import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ChatMessageRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(MessageDTO dto) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(dto.getChatRoomId());
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("보낸 사람을 찾을 수 없습니다"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(dto.getContent())
                .sentAt(System.currentTimeMillis())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // 저장된 메시지 정보로 응답 DTO 생성
        MessageResponseDTO response = MessageResponseDTO.builder()
                .messageId(saved.getMessageid())
                .chatRoomId(saved.getChatRoom().getChatroomid())
                .senderId(saved.getSender().getUserid())
                .content(saved.getContent())
                .sentAt(saved.getSentAt())
                .build();

        // ✅ 저장된 정보로 브로드캐스트!
        messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatRoomId(), response);
    }

    public List<ChatMessage> getMessagesByChatRoom(Long chatRoomId) {
        return chatMessageRepository.findByChatRoom_ChatroomidOrderBySentAtAsc(chatRoomId);
    }
}
