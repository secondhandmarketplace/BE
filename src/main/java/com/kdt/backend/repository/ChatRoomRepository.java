package com.kdt.backend.repository;

import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // ✅ 최근 등록순으로 정렬 (사용자 선호사항 [1] 반영)
    List<ChatRoom> findByBuyer_UseridOrSeller_UseridOrderByUpdatedAtDesc(String buyerId, String sellerId);

    // ✅ 생성일 기준 최근 등록순 정렬 (사용자 선호사항 [1])
    List<ChatRoom> findByBuyer_UseridOrSeller_UseridOrderByCreatedAtDesc(String buyerId, String sellerId);

    // ✅ 특정 아이템과 사용자 조합으로 채팅방 찾기
    Optional<ChatRoom> findByItemAndBuyerAndSeller(Item item, User buyer, User seller);

    // ✅ 아이템별 채팅방 조회 (최근 등록순 [1])
    List<ChatRoom> findByItemOrderByUpdatedAtDesc(Item item);

    // ✅ 활성 채팅방만 조회 (최근 등록순 [1])
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.status = 'active' ORDER BY c.updatedAt DESC")
    List<ChatRoom> findActiveChatRoomsByUser(@Param("userId") String userId);

    // ✅ 읽지 않은 메시지가 있는 채팅방 조회 (실시간 메시징 [2] 지원)
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.unreadCount > 0 ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsWithUnreadMessages(@Param("userId") String userId);

    // ✅ 특정 기간 활동한 채팅방 조회 (Java Spring 환경 [3] 반영)
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.updatedAt BETWEEN :startDate AND :endDate ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsByUserAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ✅ 구매자별 채팅방 조회 (최근 등록순 [1])
    List<ChatRoom> findByBuyer_UseridOrderByUpdatedAtDesc(String buyerId);

    // ✅ 판매자별 채팅방 조회 (최근 등록순 [1])
    List<ChatRoom> findBySeller_UseridOrderByUpdatedAtDesc(String sellerId);

    // ✅ 아이템 ID로 채팅방 조회 (최근 등록순 [1])
    List<ChatRoom> findByItem_ItemidOrderByUpdatedAtDesc(Long itemId);

    // ✅ 상태별 채팅방 조회 (최근 등록순 [1])
    List<ChatRoom> findByStatusOrderByUpdatedAtDesc(String status);

    // ✅ 채팅방 검색 (대화형 인공지능 [4] 지원)
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND LOWER(c.item.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsByUserAndItemKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    // ✅ 최근 활동 채팅방 조회 (실시간 메시징 [2] 지원)
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.updatedAt > :timestamp ORDER BY c.updatedAt DESC")
    List<ChatRoom> findRecentlyActiveChatRooms(@Param("userId") String userId, @Param("timestamp") LocalDateTime timestamp);

    // ✅ 채팅방 통계 조회
    @Query("SELECT COUNT(c) FROM ChatRoom c WHERE c.buyer.userid = :userId OR c.seller.userid = :userId")
    Long countChatRoomsByUser(@Param("userId") String userId);

    // ✅ 특정 사용자 간 채팅방 조회
    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :user1 AND c.seller.userid = :user2) OR (c.buyer.userid = :user2 AND c.seller.userid = :user1) ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    // ✅ 아이템과 사용자로 채팅방 찾기 (어느 쪽이든 구매자/판매자 가능)
    @Query("SELECT c FROM ChatRoom c WHERE c.item = :item AND ((c.buyer = :user1 AND c.seller = :user2) OR (c.buyer = :user2 AND c.seller = :user1))")
    Optional<ChatRoom> findByItemAndUsers(@Param("item") Item item, @Param("user1") User user1, @Param("user2") User user2);
}
