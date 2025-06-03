package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private Long chatroomid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_transactionid", nullable = false)
    private ItemTransaction itemTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_userid", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_userid", nullable = false)
    private User buyer;

    @Column(name = "created_at")
    private Long createdAt;
}
