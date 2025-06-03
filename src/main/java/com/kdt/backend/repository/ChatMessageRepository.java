package com.kdt.backend.repository;

import com.kdt.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_ChatroomidOrderBySentAtAsc(Long chatroomid);

    void deleteByChatRoom_Chatroomid(Long chatroomId);
    Optional<ChatMessage> findFirstByChatRoom_ChatroomidOrderBySentAtDesc(Long chatroomid);
    // ✅ 추가
    void deleteAllByChatRoom_Chatroomid(Long chatroomid);

}
