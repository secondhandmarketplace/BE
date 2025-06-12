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

    List<ChatMessage> findByChatRoom_IdOrderBySentAtDesc(Long chatRoomId);

    List<ChatMessage> findByChatRoom_IdOrderBySentAtAsc(Long chatRoomId);

    List<ChatMessage> findByChatRoom_IdAndIsReadFalseAndSender_UseridNot(Long chatRoomId, String userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.sender.userid != :userId")
    Long countUnreadMessagesByRoomAndUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);

    void deleteAllByChatRoom_Id(Long chatRoomId);

    Optional<ChatMessage> findFirstByChatRoom_IdOrderBySentAtDesc(Long chatRoomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.sentAt BETWEEN :startDate AND :endDate ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAndDateRange(
            @Param("chatRoomId") Long chatRoomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomWithPaging(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAndKeyword(@Param("chatRoomId") Long chatRoomId, @Param("keyword") String keyword);

    List<ChatMessage> findBySender_UseridOrderBySentAtDesc(String senderId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.sender.userid != :userId")
    List<ChatMessage> findUnreadMessagesByRoomAndUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.sentAt > :timestamp ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByRoomAfterTimestamp(@Param("chatRoomId") Long chatRoomId, @Param("timestamp") LocalDateTime timestamp);
}
