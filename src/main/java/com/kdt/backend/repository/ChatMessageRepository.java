package com.kdt.backend.repository;

import com.kdt.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ✅ 최근 등록순으로 정렬 (사용자 선호사항 [1] 반영)
    List<ChatMessage> findByChatRoom_IdOrderBySentAtDesc(Long chatRoomId);

    // ✅ 시간순 정렬 (오래된 순)
    List<ChatMessage> findByChatRoom_IdOrderBySentAtAsc(Long chatRoomId);

    // ✅ 읽지 않은 메시지 조회 (실시간 메시징 [2] 지원)
    List<ChatMessage> findByChatRoom_IdAndIsReadFalseAndSender_UseridNot(Long chatRoomId, String userId);

    // ✅ 특정 사용자의 읽지 않은 메시지 수
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.sender.userid != :userId")
    Long countUnreadMessagesByRoomAndUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);

    // ✅ 채팅방별 메시지 삭제
    void deleteAllByChatRoom_Id(Long chatRoomId);

    // ✅ 최신 메시지 조회 (최근 등록순 [1] 반영)
    Optional<ChatMessage> findFirstByChatRoom_IdOrderBySentAtDesc(Long chatRoomId);

    // ✅ 특정 기간 메시지 조회 (Java Spring 환경 [3] 반영)
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.sentAt BETWEEN :startDate AND :endDate ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAndDateRange(
            @Param("chatRoomId") Long chatRoomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ✅ 페이징 지원 메시지 조회 (최근 등록순 [1])
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomWithPaging(@Param("chatRoomId") Long chatRoomId);

    // ✅ 키워드 검색 (대화형 인공지능 [4] 지원)
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAndKeyword(@Param("chatRoomId") Long chatRoomId, @Param("keyword") String keyword);

    // ✅ 사용자별 메시지 조회 (최근 등록순 [1])
    List<ChatMessage> findBySender_UseridOrderBySentAtDesc(String senderId);

    // ✅ 읽음 상태 업데이트를 위한 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.sender.userid != :userId")
    List<ChatMessage> findUnreadMessagesByRoomAndUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);

    // ✅ 특정 시간 이후 메시지 조회 (실시간 메시징 [2] 지원)
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.sentAt > :timestamp ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAfterTimestamp(@Param("chatRoomId") Long chatRoomId, @Param("timestamp") LocalDateTime timestamp);
}
