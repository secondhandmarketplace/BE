package com.kdt.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room", uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_buyer_seller_item",
                columnNames = {"buyer_userid", "seller_userid", "item_transaction_id"}
        )
})
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_transaction_id", nullable = false)
    @JsonIgnore
    private Item itemTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_userid", referencedColumnName = "userid", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_userid", referencedColumnName = "userid", nullable = false)
    private User buyer;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "status")
    private String status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.unreadCount == null) {
            this.unreadCount = 0;
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
