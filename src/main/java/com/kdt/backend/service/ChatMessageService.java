package com.kdt.backend.service;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ChatMessageRepository;
import com.kdt.backend.repository.ChatRoomRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository; // ✅ ChatRoom 엔티티 조회를 위해 필요
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ 메시지 전송 (최근 등록순 정렬 반영)
     */
    public ChatMessageDTO sendMessage(ChatMessageDTO dto, String currentUserId) {
        try {
            log.info("메시지 전송 시작: roomId={}, senderId={}", dto.getChatRoomId(), dto.getSenderId());

            // ✅ 메시지 전송자는 반드시 현재 로그인 사용자여야 하므로, currentUserId == dto.getSenderId() 검증 필요
            if (!currentUserId.equals(dto.getSenderId())) {
                throw new RuntimeException("메시지 전송 권한이 없습니다");
            }

            // ✅ ChatRoom 엔티티 조회 (DTO가 아닌 엔티티로 저장해야 함)
            ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                    .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
            User sender = userRepository.findById(dto.getSenderId())
                    .orElseThrow(() -> new RuntimeException("보낸 사람을 찾을 수 없습니다"));

            // ✅ LocalDateTime 사용
            ChatMessage message = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .content(dto.getContent())
                    .sentAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            ChatMessage saved = chatMessageRepository.save(message);

            // ✅ 채팅방 최신 메시지 업데이트 (ChatRoom 엔티티는 따로 저장 필요)
            chatRoom.setLastMessage(dto.getContent());
            chatRoom.setUpdatedAt(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);

            // ✅ 응답 DTO 생성
            ChatMessageDTO response = ChatMessageDTO.builder()
                    .messageId(saved.getId())
                    .chatRoomId(saved.getChatRoom().getId())
                    .senderId(saved.getSender().getUserid())
                    .senderName(saved.getSender().getName())
                    .content(saved.getContent())
                    .sentAt(saved.getSentAt())
                    .isRead(saved.getIsRead())
                    .build();

            // ✅ WebSocket 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatRoomId(), response);

            log.info("메시지 전송 완료: messageId={}", saved.getId());
            return response;

        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage());
            throw new RuntimeException("메시지 전송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ✅ 채팅방 메시지 조회 (최근 등록순 정렬 반영)
     */
    public List<ChatMessageDTO> getMessagesByChatRoom(Long chatRoomId) {
        try {
            // ✅ 최근 등록순으로 정렬
            List<ChatMessage> messages = chatMessageRepository.findByChatRoom_IdOrderBySentAtDesc(chatRoomId);

            return messages.stream()
                    .map(this::convertToChatMessageDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("메시지 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 읽지 않은 메시지 읽음 처리
     */
    public void markMessagesAsRead(Long chatRoomId, String userId) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepository
                    .findByChatRoom_IdAndIsReadFalseAndSender_UseridNot(chatRoomId, userId);

            unreadMessages.forEach(message -> message.setIsRead(true));
            chatMessageRepository.saveAll(unreadMessages);

            log.info("읽음 처리 완료: roomId={}, 처리된 메시지 수={}", chatRoomId, unreadMessages.size());

        } catch (Exception e) {
            log.error("읽음 처리 실패: {}", e.getMessage());
        }
    }

    /**
     * ✅ 메시지 삭제
     */
    public void deleteMessage(Long messageId, String userId) {
        try {
            ChatMessage message = chatMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다"));

            // 권한 확인
            if (!message.getSender().getUserid().equals(userId)) {
                throw new RuntimeException("메시지 삭제 권한이 없습니다");
            }

            chatMessageRepository.delete(message);
            log.info("메시지 삭제 완료: messageId={}", messageId);

        } catch (Exception e) {
            log.error("메시지 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("메시지 삭제에 실패했습니다");
        }
    }

    /**
     * ✅ 변환 메서드
     */
    private ChatMessageDTO convertToChatMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getSender().getUserid())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .build();
    }
}
