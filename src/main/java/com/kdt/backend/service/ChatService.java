package com.kdt.backend.service;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.User;
import com.kdt.backend.exception.ChatException;
import com.kdt.backend.repository.ChatRoomRepository;
import com.kdt.backend.repository.ChatMessageRepository;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.UserRepository;
import com.kdt.backend.util.ChatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public ChatRoomResponseDTO createChatRoom(String userid, String otherUserId, Long itemId) {
        try {
            log.info("채팅방 생성 서비스: userid={}, otherUserId={}, itemId={}", userid, otherUserId, itemId);

            // ✅ 엔티티 조회 및 검증
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new ChatException("상품을 찾을 수 없습니다: " + itemId));

            User user = userRepository.findByUserid(userid)
                    .orElseThrow(() -> new ChatException.UserNotFoundException(userid));
            User otherUser = userRepository.findByUserid(otherUserId)
                    .orElseThrow(() -> new ChatException.UserNotFoundException(otherUserId));

            // ✅ 기존 채팅방 확인 (양방향 검색)
            ChatRoom existingRoom = chatRoomRepository.findByItemAndUsers(item, user, otherUser)
                    .orElse(null);

            if (existingRoom != null) {
                log.info("기존 채팅방 반환: roomId={}", existingRoom.getId());
                return convertToChatRoomResponseDTO(existingRoom, userid);
            }

            // ✅ 새 채팅방 생성 (최근 등록순 [3] 반영)
            ChatRoom newRoom = ChatRoom.builder()
                    .itemTransaction(item)
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
            ChatRoomResponseDTO responseDTO = convertToChatRoomResponseDTO(savedRoom, userid);
            messagingTemplate.convertAndSend("/topic/chatroom/" + otherUserId, responseDTO);

            return responseDTO;

        } catch (ChatException e) {
            log.error("채팅방 생성 서비스 실패: {}", e.getMessage(), e);
            throw e; // 커스텀 예외는 그대로 전파
        } catch (Exception e) {
            log.error("채팅방 생성 서비스 실패: {}", e.getMessage(), e);
            throw new ChatException.ChatRoomCreationException(e.getMessage(), e);
        }
    }

    /**
     * ✅ 사용자별 채팅방 목록 조회 (최근 등록순 [3] 정렬)
     */
    public List<ChatRoomResponseDTO> getChatRoomsByUserId(String userid) {
        try {
            log.info("채팅방 목록 조회: userid={}", userid);

            if (userid == null || userid.trim().isEmpty()) {
                return List.of();
            }

            // ✅ 사용자 존재 확인
            boolean userExists = userRepository.existsByUserid(userid);
            if (!userExists) {
                log.warn("존재하지 않는 사용자: {}", userid);
                return List.of();
            }

            // ✅ 최근 등록순으로 정렬 (사용자 선호사항 [3])
            List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUser(userid);

            List<ChatRoomResponseDTO> result = chatRooms.stream()
                    .map(room -> convertToChatRoomResponseDTO(room, userid))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            log.info("채팅방 목록 조회 완료: {}개 (최근 등록순)", result.size());
            return result;

        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: userid={}, error={}", userid, e.getMessage(), e);
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
                    .orElseThrow(() -> new ChatException.ChatRoomNotFoundException(roomId));

            User sender = userRepository.findByUserid(messageDTO.getSenderId())
                    .orElseThrow(() -> new ChatException.UserNotFoundException(messageDTO.getSenderId()));

            // ✅ 메시지 내용 검증 및 정리
            if (!ChatUtils.isValidMessage(messageDTO.getContent())) {
                throw new ChatException.MessageSendFailedException("유효하지 않은 메시지 내용입니다.");
            }

            // ✅ 채팅방 접근 권한 확인
            if (!ChatUtils.hasAccessToChatRoom(chatRoom, messageDTO.getSenderId())) {
                throw new ChatException.UnauthorizedAccessException("채팅방 접근 권한이 없습니다.");
            }

            // ✅ 메시지 저장 (Java Spring [4] 환경)
            ChatMessage message = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .content(ChatUtils.sanitizeMessage(messageDTO.getContent()))
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
            messagingTemplate.convertAndSend(ChatUtils.createChatTopic(roomId), responseDTO);

            // ✅ 상대방에게 알림 전송
            messagingTemplate.convertAndSend(ChatUtils.createUserNotificationTopic(otherUserId), responseDTO);

            log.info("메시지 전송 완료: messageId={}", savedMessage.getId());
            return responseDTO;

        } catch (ChatException e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            throw e; // 커스텀 예외는 그대로 전파
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            throw new ChatException.MessageSendFailedException(e.getMessage(), e);
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
    public boolean markMessagesAsRead(Long roomId, String userid) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepository
                    .findUnreadMessagesByRoomAndUser(roomId, userid);

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
     * ✅ 채팅방 정보 조회 (단일 채팅방)
     */
    public ChatRoomResponseDTO getChatRoomById(Long roomId, String userId) {
        try {
            log.info("채팅방 정보 조회: roomId={}, userId={}", roomId, userId);

            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(roomId);
            if (chatRoomOpt.isEmpty()) {
                log.warn("채팅방을 찾을 수 없음: roomId={}", roomId);
                return null;
            }

            ChatRoom chatRoom = chatRoomOpt.get();

            // ✅ 권한 확인: 구매자 또는 판매자만 조회 가능
            if (!chatRoom.getBuyer().getUserid().equals(userId) && !chatRoom.getSeller().getUserid().equals(userId)) {
                log.warn("채팅방 조회 권한 없음: roomId={}, userId={}, buyerId={}, sellerId={}",
                        roomId, userId, chatRoom.getBuyer().getUserid(), chatRoom.getSeller().getUserid());
                return null;
            }

            // ✅ 상대방 정보 가져오기
            User otherUser = ChatUtils.getOtherUser(chatRoom, userId);

            // ✅ 아이템 정보 가져오기
            Item item = chatRoom.getItemTransaction();

            // ✅ DTO 생성
            ChatRoomResponseDTO.ChatRoomResponseDTOBuilder builder = ChatUtils.createChatRoomBuilder(chatRoom, userId);

            if (item != null) {
                builder.itemTransactionId(item.getItemid()) // itemTransactionId 추가
                        .itemId(item.getItemid())
                        .itemTitle(item.getTitle())
                        .itemPrice(item.getPrice())
                        .itemImageUrl(item.getThumbnail());
            }

            ChatRoomResponseDTO result = builder.build();
            log.info("채팅방 정보 조회 완료: roomId={}, otherUserId={}", roomId, result.getOtherUserId());
            return result;

        } catch (Exception e) {
            log.error("채팅방 정보 조회 실패: roomId={}, userId={}, error={}", roomId, userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * ✅ 채팅방 삭제
     */
    public boolean deleteChatRoom(Long roomId, String userid) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new ChatException.ChatRoomNotFoundException(roomId));

            // ✅ 권한 확인
            if (!chatRoom.getBuyer().getUserid().equals(userid) &&
                    !chatRoom.getSeller().getUserid().equals(userid)) {
                throw new ChatException.UnauthorizedAccessException("채팅방 삭제");
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
                    .otherUserId(otherUserId)
                    .otherUserName(otherUserName)
                    .lastMessage(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "메시지가 없습니다.")
                    .updatedAt(chatRoom.getUpdatedAt())
                    .itemImageUrl(chatRoom.getItemTransaction() != null ? chatRoom.getItemTransaction().getFirstImagePath() : "/assets/default-image.png")
                    .itemId(chatRoom.getItemTransaction() != null ? chatRoom.getItemTransaction().getItemid() : null)
                    .itemTitle(chatRoom.getItemTransaction() != null ? chatRoom.getItemTransaction().getTitle() : "상품명 없음")
                    .itemPrice(chatRoom.getItemTransaction() != null ? chatRoom.getItemTransaction().getPrice() : 0)
                    .unreadCount(chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0)
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
