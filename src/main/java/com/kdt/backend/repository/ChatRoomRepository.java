package com.kdt.backend.repository;

import com.kdt.backend.entity.ChatMessage;
import com.kdt.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByBuyer_UseridOrSeller_Userid(String buyerId, String sellerId);
    List<ChatRoom> findAllByItemTransaction_Transactionid(Long transactionId);

    Optional<ChatRoom> findByItemTransaction_Transactionid(Long transactionId);
    void deleteByItemTransaction_Transactionid(Long transactionId); // ✅ 이거 추가!
}
