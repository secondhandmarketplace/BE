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

    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.buyer.userid = :buyerId " +
            "AND cr.seller.userid = :sellerId " +
            "AND cr.itemTransaction.itemid = :itemTransactionId")
    List<ChatRoom> findChatRooms(@Param("buyerId") String buyerId,
                                 @Param("sellerId") String sellerId,
                                 @Param("itemTransactionId") Long itemTransactionId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.buyer.userid = :userId OR cr.seller.userid = :userId ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findChatRoomsByUser(@Param("userId") String userId);

    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :user1Id AND c.seller.userid = :user2Id) OR (c.buyer.userid = :user2Id AND c.seller.userid = :user1Id) ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsBetweenUsers(@Param("user1Id") String user1Id, @Param("user2Id") String user2Id);

    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND LOWER(c.itemTransaction.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsByUserAndItemKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.updatedAt BETWEEN :startDate AND :endDate ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsByUserAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM ChatRoom c WHERE (c.buyer.userid = :userId OR c.seller.userid = :userId) AND c.unreadCount > 0 ORDER BY c.updatedAt DESC")
    List<ChatRoom> findChatRoomsWithUnreadMessages(@Param("userId") String userId);

    @Query("SELECT COUNT(c) FROM ChatRoom c WHERE c.buyer.userid = :userId OR c.seller.userid = :userId")
    Long countChatRoomsByUser(@Param("userId") String userId);

    @Query("SELECT c FROM ChatRoom c WHERE c.itemTransaction = :item AND ((c.buyer = :user1 AND c.seller = :user2) OR (c.buyer = :user2 AND c.seller = :user1))")
    Optional<ChatRoom> findByItemAndUsers(@Param("item") Item item, @Param("user1") User user1, @Param("user2") User user2);
}
