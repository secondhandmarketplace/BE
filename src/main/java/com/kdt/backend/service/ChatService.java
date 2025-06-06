package com.kdt.backend.service;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ChatRoomRepository;
import com.kdt.backend.repository.ChatMessageRepository;
import com.kdt.backend.repository.ItemRepository;
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
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ 채팅방 생성 (Java Spring [4] 환경 + 실시간 메시징 [1] 지원)
     */
    public ChatRoomResponseDTO createChatRoom(String userId, String otherUserId, Long itemId) {
        try {
            log.info("채팅방 생성 서비스: userId={}, otherUserId={}, itemId={}", userId, otherUserId, itemId);

            // ✅ 엔티티 조회 및 검증
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + itemId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("상대방을 찾을 수 없습니다: " + otherUserId));

            // ✅ 기존 채팅방 확인 (양방향 검색)
            ChatRoom existingRoom = chatRoomRepository.findByItemAndUsers(item, user, otherUser)
                    .orElse(null);

            if (existingRoom != null) {
                log.info("기존 채팅방 반환: roomId={}", existingRoom.getId());
                return convertToChatRoomResponseDTO(existingRoom, userId);
            }

            // ✅ 새 채팅방 생성 (최근 등록순 [3] 반영)
            ChatRoom newRoom = ChatRoom.builder()
                    .item(item)
                    .buyer(user)
                    .seller(otherUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status("active")
                    .unreadCount(0)
                    .lastMessage("채팅방이 생성되었습니다.")
                    .build();

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            log.info("새 채팅방 생성 완료: roomId={}", savedRoom.getId());

            // ✅ 실시간 알림 전송 (실시간 메시징 [1] 지원)
            ChatRoomResponseDTO responseDTO = convertToChatRoomResponseDTO(savedRoom, userId);
            messagingTemplate.convertAndSend("/topic/chatroom/" + otherUserId, responseDTO);

            return responseDTO;

        } catch (Exception e) {
            log.error("채팅방 생성 서비스 실패: {}", e.getMessage(), e);
            throw new RuntimeException("채팅방 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ✅ 사용자별 채팅방 목록 조회 (최근 등록순 [3] 정렬)
     */
    public List<ChatRoomResponseDTO> getChatRoomsByUserId(String userId) {
        try {
            log.info("채팅방 목록 조회: userId={}", userId);

            if (userId == null || userId.trim().isEmpty()) {
                return List.of();
            }

            // ✅ 사용자 존재 확인
            boolean userExists = userRepository.existsById(userId);
            if (!userExists) {
                log.warn("존재하지 않는 사용자: {}", userId);
                return List.of();
            }

            // ✅ 최근 등록순으로 정렬 (사용자 선호사항 [3])
            List<ChatRoom> chatRooms = chatRoomRepository.findByBuyer_UseridOrSeller_UseridOrderByUpdatedAtDesc(userId, userId);

            List<ChatRoomResponseDTO> result = chatRooms.stream()
                    .map(room -> convertToChatRoomResponseDTO(room, userId))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            log.info("채팅방 목록 조회 완료: {}개 (최근 등록순)", result.size());
            return result;

        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * ✅ 메시지 전송 (실시간 메시징 [1] + 대화형 인공지능 [2] 지원)
     */
    public ChatMessageDTO sendMessage(Long roomId, ChatMessageDTO messageDTO) {
        try {
            log.info("메시지 전송: roomId={}, senderId={}", roomId, messageDTO.getSenderId());

            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

            User sender = userRepository.findById(messageDTO.getSenderId())
                    .orElseThrow(() -> new RuntimeException("발신자를 찾을 수 없습니다."));

            // ✅ 메시지 저장 (Java Spring [4] 환경)
            ChatMessage message = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .content(messageDTO.getContent())
                    .sentAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            ChatMessage savedMessage = chatMessageRepository.save(message);

            // ✅ 채팅방 업데이트 (최근 등록순 [3] 반영)
            chatRoom.setLastMessage(messageDTO.getContent());
            chatRoom.setUpdatedAt(LocalDateTime.now());

            // ✅ 읽지 않은 메시지 수 증가
            String otherUserId = getOtherUserId(chatRoom, messageDTO.getSenderId());
            if (otherUserId != null) {
                chatRoom.setUnreadCount((chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0) + 1);
            }

            chatRoomRepository.save(chatRoom);

            // ✅ DTO 변환
            ChatMessageDTO responseDTO = convertToChatMessageDTO(savedMessage);

            // ✅ 실시간 WebSocket 브로드캐스트 (실시간 메시징 [1])
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, responseDTO);

            log.info("메시지 전송 완료: messageId={}", savedMessage.getId());
            return responseDTO;

        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 전송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ✅ 채팅 메시지 조회 (최근 등록순 [3] 반영)
     */
    public List<ChatMessageDTO> getChatMessages(Long roomId, int page, int size) {
        try {
            // ✅ 시간순 정렬 (오래된 순 - 채팅 특성상)
            List<ChatMessage> messages = chatMessageRepository.findByChatRoom_IdOrderBySentAtAsc(roomId);

            return messages.stream()
                    .skip(page * size)
                    .limit(size)
                    .map(this::convertToChatMessageDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("채팅 메시지 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 읽음 처리 (실시간 메시징 [1] 지원)
     */
    public boolean markMessagesAsRead(Long roomId, String userId) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepository
                    .findUnreadMessagesByRoomAndUser(roomId, userId);

            unreadMessages.forEach(message -> message.setIsRead(true));
            chatMessageRepository.saveAll(unreadMessages);

            // ✅ 채팅방 읽지 않은 메시지 수 초기화
            ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
            if (chatRoom != null) {
                chatRoom.setUnreadCount(0);
                chatRoomRepository.save(chatRoom);
            }

            log.info("읽음 처리 완료: roomId={}, 처리된 메시지 수={}", roomId, unreadMessages.size());
            return true;

        } catch (Exception e) {
            log.error("읽음 처리 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 채팅방 삭제
     */
    public boolean deleteChatRoom(Long roomId, String userId) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

            // ✅ 권한 확인
            if (!chatRoom.getBuyer().getUserid().equals(userId) &&
                    !chatRoom.getSeller().getUserid().equals(userId)) {
                throw new RuntimeException("채팅방 삭제 권한이 없습니다.");
            }

            // ✅ 메시지 먼저 삭제
            chatMessageRepository.deleteAllByChatRoom_Id(roomId);

            // ✅ 채팅방 삭제
            chatRoomRepository.deleteById(roomId);

            log.info("채팅방 삭제 완료: roomId={}", roomId);
            return true;

        } catch (Exception e) {
            log.error("채팅방 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    // ===== 변환 메서드들 (대화형 인공지능 [2] 지원) =====

    /**
     * ✅ ChatRoom -> ChatRoomResponseDTO 변환 (현재 사용자 기준)
     */
    private ChatRoomResponseDTO convertToChatRoomResponseDTO(ChatRoom chatRoom, String currentUserId) {
        try {
            if (chatRoom == null) {
                return null;
            }

            // ✅ 상대방 정보 결정 (대화형 인공지능 [2] 지원)
            String otherUserName;
            String otherUserId;

            if (currentUserId.equals(chatRoom.getBuyer().getUserid())) {
                // 현재 사용자가 구매자라면 판매자 정보
                otherUserName = chatRoom.getSeller().getName();
                otherUserId = chatRoom.getSeller().getUserid();
            } else {
                // 현재 사용자가 판매자라면 구매자 정보
                otherUserName = chatRoom.getBuyer().getName();
                otherUserId = chatRoom.getBuyer().getUserid();
            }

            return ChatRoomResponseDTO.builder()
                    .id(chatRoom.getId())
                    .roomId(chatRoom.getId())
                    .nickname(otherUserName)
                    .otherUserName(otherUserName)
                    .lastMessage(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "메시지가 없습니다.")
                    .lastTimestamp(chatRoom.getUpdatedAt())
                    .updatedAt(chatRoom.getUpdatedAt())
                    .itemImageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : "/assets/default-image.png")
                    .imageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : "/assets/default-image.png")
                    .itemId(chatRoom.getItem() != null ? chatRoom.getItem().getItemid() : null)
                    .itemTitle(chatRoom.getItem() != null ? chatRoom.getItem().getTitle() : "상품명 없음")
                    .itemPrice(chatRoom.getItem() != null ? chatRoom.getItem().getPrice() : 0)
                    .unreadCount(chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0)
                    .otherUserId(otherUserId)
                    .status(chatRoom.getStatus() != null ? chatRoom.getStatus() : "active")
                    .build();

        } catch (Exception e) {
            log.error("DTO 변환 실패: chatRoomId={}, error={}", chatRoom != null ? chatRoom.getId() : "null", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ ChatMessage -> ChatMessageDTO 변환
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

    /**
     * ✅ 상대방 사용자 ID 조회
     */
    private String getOtherUserId(ChatRoom chatRoom, String currentUserId) {
        if (currentUserId.equals(chatRoom.getBuyer().getUserid())) {
            return chatRoom.getSeller().getUserid();
        } else {
            return chatRoom.getBuyer().getUserid();
        }
    }
}
