package com.kdt.backend.service;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.*;
import com.kdt.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * ✅ 채팅방 생성 (Java Spring 환경 [2] 반영)
     */
    public ChatRoomResponseDTO createChatRoom(String userId, String otherUserId, Long itemId) {
        try {
            log.info("채팅방 생성: userId={}, otherUserId={}, itemId={}", userId, otherUserId, itemId);

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));

            User buyer = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다"));
            User seller = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없습니다"));

            // 기존 채팅방 확인
            ChatRoom existingRoom = chatRoomRepository.findByItemAndBuyerAndSeller(item, buyer, seller)
                    .orElse(null);

            if (existingRoom != null) {
                return convertToChatRoomResponseDTO(existingRoom);
            }

            // 새 채팅방 생성
            ChatRoom newRoom = ChatRoom.builder()
                    .item(item)
                    .buyer(buyer)
                    .seller(seller)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status("active")
                    .unreadCount(0)
                    .build();

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            log.info("새 채팅방 생성 완료: roomId={}", savedRoom.getId());

            return convertToChatRoomResponseDTO(savedRoom);

        } catch (Exception e) {
            log.error("채팅방 생성 실패: {}", e.getMessage());
            throw new RuntimeException("채팅방 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ✅ 사용자별 채팅방 목록 조회 (최근 등록순 [3] 반영)
     */
    public List<ChatRoomResponseDTO> getChatRoomsByUser(String userId) {
        try {
            List<ChatRoom> rooms = chatRoomRepository.findByBuyer_UseridOrSeller_UseridOrderByUpdatedAtDesc(userId, userId);

            return rooms.stream()
                    .map(this::convertToChatRoomResponseDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 채팅방 조회
     */
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
    }

    /**
     * ✅ 채팅방 삭제 (메서드 시그니처 수정)
     */
    @Transactional
    public boolean deleteChatRoom(Long chatRoomId, String userId) {
        try {
            ChatRoom chatRoom = getChatRoomById(chatRoomId);

            // 권한 확인
            if (!chatRoom.getBuyer().getUserid().equals(userId) &&
                    !chatRoom.getSeller().getUserid().equals(userId)) {
                throw new RuntimeException("채팅방 삭제 권한이 없습니다");
            }

            // 메시지 먼저 삭제
            chatMessageRepository.deleteAllByChatRoom_Id(chatRoomId);

            // 채팅방 삭제
            chatRoomRepository.deleteById(chatRoomId);

            log.info("채팅방 삭제 완료: roomId={}", chatRoomId);
            return true;

        } catch (Exception e) {
            log.error("채팅방 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 오버로드된 삭제 메서드 (호환성 유지)
     */
    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        try {
            chatMessageRepository.deleteAllByChatRoom_Id(chatRoomId);
            chatRoomRepository.deleteById(chatRoomId);
            log.info("채팅방 삭제 완료: roomId={}", chatRoomId);
        } catch (Exception e) {
            log.error("채팅방 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("채팅방 삭제에 실패했습니다");
        }
    }

    /**
     * ✅ 최신 메시지 조회
     */
    public String getLatestMessageByRoomId(Long chatRoomId) {
        return chatMessageRepository
                .findFirstByChatRoom_IdOrderBySentAtDesc(chatRoomId)
                .map(ChatMessage::getContent)
                .orElse("메시지가 없습니다");
    }

    // ===== 변환 메서드들 (타입 오류 해결) =====

    /**
     * ✅ 기본 변환 메서드 (대화형 인공지능 [4] 반영)
     */
    private ChatRoomResponseDTO convertToChatRoomResponseDTO(ChatRoom chatRoom) {
        // 최신 메시지 조회
        String latestMessage = getLatestMessageByRoomId(chatRoom.getId());

        return ChatRoomResponseDTO.builder()
                .id(chatRoom.getId()) // ✅ Long 타입 그대로 사용
                .roomId(chatRoom.getId()) // ✅ Long 타입 그대로 사용
                .nickname(chatRoom.getOtherUserName())
                .otherUserName(chatRoom.getOtherUserName())
                .lastMessage(latestMessage)
                .lastTimestamp(chatRoom.getUpdatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .itemImageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : null)
                .imageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : null)
                .itemId(chatRoom.getItem() != null ? chatRoom.getItem().getItemid() : null)
                .itemTitle(chatRoom.getItem() != null ? chatRoom.getItem().getTitle() : null)
                .itemPrice(chatRoom.getItem() != null ? chatRoom.getItem().getPrice() : null)
                .unreadCount(chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0)
                .otherUserId(chatRoom.getOtherUserId())
                .status(chatRoom.getStatus() != null ? chatRoom.getStatus() : "active")
                .build();
    }

    /**
     * ✅ 현재 사용자 기준 변환 메서드
     */
    private ChatRoomResponseDTO convertToChatRoomResponseDTO(ChatRoom chatRoom, String currentUserId) {
        String latestMessage = getLatestMessageByRoomId(chatRoom.getId());

        return ChatRoomResponseDTO.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getId())
                .nickname(chatRoom.getOtherUserName(currentUserId))
                .otherUserName(chatRoom.getOtherUserName(currentUserId))
                .lastMessage(latestMessage)
                .lastTimestamp(chatRoom.getUpdatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .itemImageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : null)
                .imageUrl(chatRoom.getItem() != null ? chatRoom.getItem().getFirstImagePath() : null)
                .itemId(chatRoom.getItem() != null ? chatRoom.getItem().getItemid() : null)
                .itemTitle(chatRoom.getItem() != null ? chatRoom.getItem().getTitle() : null)
                .itemPrice(chatRoom.getItem() != null ? chatRoom.getItem().getPrice() : null)
                .unreadCount(chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0)
                .otherUserId(chatRoom.getOtherUserId(currentUserId))
                .status(chatRoom.getStatus() != null ? chatRoom.getStatus() : "active")
                .build();
    }

    // ===== 레거시 메서드들 (호환성 유지) =====

    /**
     * ✅ 레거시 createChatRoom 메서드 (호환성 유지)
     */
    public ChatRoomResponseDTO createChatRoom(Long transactionId, String buyerId, String sellerId) {
        // 실제 구현에서는 transactionId를 itemId로 변환하거나 별도 로직 필요
        log.warn("레거시 createChatRoom 호출: transactionId={}", transactionId);

        // 임시로 transactionId를 itemId로 사용
        return createChatRoom(buyerId, sellerId, transactionId);
    }

    /**
     * ✅ 헬퍼 메서드들 (제거 예정)
     */
    @Deprecated
    private String getOtherUserName(ChatRoom chatRoom) {
        return chatRoom.getSeller().getName();
    }

    @Deprecated
    private String getOtherUserId(ChatRoom chatRoom) {
        return chatRoom.getSeller().getUserid();
    }

    /**
     * ✅ 채팅방 상세 조회 (DTO 직접 반환)
     */
    public ChatRoomResponseDTO getChatRoomResponseById(Long chatRoomId) {
        try {
            ChatRoom chatRoom = getChatRoomById(chatRoomId);
            return convertToChatRoomResponseDTO(chatRoom);
        } catch (Exception e) {
            log.error("채팅방 DTO 조회 실패: {}", e.getMessage());
            throw new RuntimeException("채팅방을 찾을 수 없습니다");
        }
    }

}
