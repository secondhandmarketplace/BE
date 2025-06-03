package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroomid", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_userid", nullable = false)
    private User sender;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sent_at")
    private Long sentAt;

    @Column(name = "is_read")
    private Boolean isRead = false;
}
