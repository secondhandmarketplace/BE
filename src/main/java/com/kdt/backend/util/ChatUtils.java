package com.kdt.backend.util;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * ✅ 채팅 관련 유틸리티 클래스
 */
@Slf4j
public class ChatUtils {

    private static final Pattern PROFANITY_PATTERN = Pattern.compile(
            "(욕설|비속어|금지어)", Pattern.CASE_INSENSITIVE
    );
    
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 메시지 내용 검증
     */
    public static boolean isValidMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        if (content.length() > MAX_MESSAGE_LENGTH) {
            log.warn("메시지 길이 초과: {} characters", content.length());
            return false;
        }
        
        if (PROFANITY_PATTERN.matcher(content).find()) {
            log.warn("부적절한 내용 감지");
            return false;
        }
        
        return true;
    }

    /**
     * 메시지 내용 정리 (HTML 태그 제거, 공백 정리)
     */
    public static String sanitizeMessage(String content) {
        if (content == null) {
            return "";
        }
        
        // HTML 태그 제거
        String sanitized = content.replaceAll("<[^>]*>", "");
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // 앞뒤 공백 제거
        return sanitized.trim();
    }

    /**
     * 채팅방에서 상대방 사용자 정보 추출
     */
    public static User getOtherUser(ChatRoom chatRoom, String currentUserId) {
        if (chatRoom.getBuyer().getUserid().equals(currentUserId)) {
            return chatRoom.getSeller();
        } else {
            return chatRoom.getBuyer();
        }
    }

    /**
     * 채팅방 참여 권한 확인
     */
    public static boolean hasAccessToChatRoom(ChatRoom chatRoom, String userId) {
        return chatRoom.getBuyer().getUserid().equals(userId) || 
               chatRoom.getSeller().getUserid().equals(userId);
    }

    /**
     * 메시지 시간 포맷팅
     */
    public static String formatMessageTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 같은 날이면 시간만 표시
        if (dateTime.toLocalDate().equals(now.toLocalDate())) {
            return dateTime.format(TIME_FORMATTER);
        }
        
        // 다른 날이면 날짜 표시
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 읽지 않은 메시지 수 계산
     */
    public static int calculateUnreadCount(ChatRoom chatRoom, String userId) {
        // 실제 구현에서는 데이터베이스 쿼리를 통해 계산
        // 여기서는 기본값 반환
        return chatRoom.getUnreadCount() != null ? chatRoom.getUnreadCount() : 0;
    }

    /**
     * 채팅방 상태 검증
     */
    public static boolean isChatRoomActive(ChatRoom chatRoom) {
        return chatRoom != null && 
               "ACTIVE".equalsIgnoreCase(chatRoom.getStatus()) &&
               chatRoom.getBuyer() != null &&
               chatRoom.getSeller() != null &&
               chatRoom.getItemTransaction() != null;
    }

    /**
     * 메시지 DTO 빌더 헬퍼
     */
    public static ChatMessageDTO.ChatMessageDTOBuilder createMessageBuilder(
            Long chatRoomId, String senderId, String content) {
        return ChatMessageDTO.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .content(sanitizeMessage(content))
                .sentAt(LocalDateTime.now())
                .isRead(false);
    }

    /**
     * 채팅방 응답 DTO 빌더 헬퍼
     */
    public static ChatRoomResponseDTO.ChatRoomResponseDTOBuilder createChatRoomBuilder(
            ChatRoom chatRoom, String currentUserId) {
        User otherUser = getOtherUser(chatRoom, currentUserId);
        
        return ChatRoomResponseDTO.builder()
                .id(chatRoom.getId())
                .otherUserId(otherUser.getUserid())
                .otherUserName(otherUser.getName())
                .lastMessage(chatRoom.getLastMessage() != null ? 
                    chatRoom.getLastMessage() : "대화를 시작해보세요.")
                .updatedAt(chatRoom.getUpdatedAt())
                .unreadCount(calculateUnreadCount(chatRoom, currentUserId))
                .status(chatRoom.getStatus() != null ? chatRoom.getStatus() : "ACTIVE");
    }

    /**
     * WebSocket 토픽 생성
     */
    public static String createChatTopic(Long chatRoomId) {
        return "/topic/chat/" + chatRoomId;
    }

    /**
     * 사용자별 알림 토픽 생성
     */
    public static String createUserNotificationTopic(String userId) {
        return "/topic/user/" + userId + "/notifications";
    }

    /**
     * 채팅방 알림 토픽 생성
     */
    public static String createChatRoomNotificationTopic(String userId) {
        return "/topic/chatroom/" + userId;
    }

    /**
     * 메시지 미리보기 생성 (긴 메시지 축약)
     */
    public static String createMessagePreview(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength - 3) + "...";
    }

    /**
     * 채팅방 제목 생성
     */
    public static String createChatRoomTitle(ChatRoom chatRoom, String currentUserId) {
        User otherUser = getOtherUser(chatRoom, currentUserId);
        String itemTitle = chatRoom.getItemTransaction() != null ? 
            chatRoom.getItemTransaction().getTitle() : "상품";
        
        return String.format("%s님과의 %s 채팅", otherUser.getName(), itemTitle);
    }

    /**
     * 에러 메시지 생성
     */
    public static String createErrorMessage(String operation, String reason) {
        return String.format("%s 중 오류가 발생했습니다: %s", operation, reason);
    }
}
