package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_userid", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_userid", nullable = false)
    private User buyer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "status")
    private String status;

    // ✅ 누락된 메서드들 추가 (Java Spring 환경 [1] 반영)

    /**
     * ✅ 상대방 사용자 이름 반환
     */
    public String getOtherUserName() {
        // 실제 구현에서는 현재 로그인한 사용자에 따라 상대방 반환
        // 여기서는 판매자 이름을 기본값으로 반환
        return this.seller != null ? this.seller.getName() : "알 수 없음";
    }

    /**
     * ✅ 현재 사용자 기준 상대방 사용자 이름 반환
     */
    public String getOtherUserName(String currentUserId) {
        if (currentUserId == null) {
            return getOtherUserName();
        }

        if (currentUserId.equals(this.buyer.getUserid())) {
            // 현재 사용자가 구매자라면 판매자 이름 반환
            return this.seller != null ? this.seller.getName() : "판매자";
        } else if (currentUserId.equals(this.seller.getUserid())) {
            // 현재 사용자가 판매자라면 구매자 이름 반환
            return this.buyer != null ? this.buyer.getName() : "구매자";
        } else {
            // 현재 사용자가 채팅방 참여자가 아닌 경우
            return "알 수 없음";
        }
    }

    /**
     * ✅ 상대방 사용자 ID 반환
     */
    public String getOtherUserId() {
        // 기본값으로 판매자 ID 반환
        return this.seller != null ? this.seller.getUserid() : null;
    }

    /**
     * ✅ 현재 사용자 기준 상대방 사용자 ID 반환
     */
    public String getOtherUserId(String currentUserId) {
        if (currentUserId == null) {
            return getOtherUserId();
        }

        if (currentUserId.equals(this.buyer.getUserid())) {
            // 현재 사용자가 구매자라면 판매자 ID 반환
            return this.seller != null ? this.seller.getUserid() : null;
        } else if (currentUserId.equals(this.seller.getUserid())) {
            // 현재 사용자가 판매자라면 구매자 ID 반환
            return this.buyer != null ? this.buyer.getUserid() : null;
        } else {
            // 현재 사용자가 채팅방 참여자가 아닌 경우
            return null;
        }
    }

    /**
     * ✅ 채팅방 참여자 확인
     */
    public boolean isParticipant(String userId) {
        if (userId == null) return false;

        return userId.equals(this.buyer.getUserid()) ||
                userId.equals(this.seller.getUserid());
    }

    /**
     * ✅ 구매자인지 확인
     */
    public boolean isBuyer(String userId) {
        return userId != null && userId.equals(this.buyer.getUserid());
    }

    /**
     * ✅ 판매자인지 확인
     */
    public boolean isSeller(String userId) {
        return userId != null && userId.equals(this.seller.getUserid());
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.unreadCount == null) {
            this.unreadCount = 0;
        }
        if (this.status == null) {
            this.status = "active";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
