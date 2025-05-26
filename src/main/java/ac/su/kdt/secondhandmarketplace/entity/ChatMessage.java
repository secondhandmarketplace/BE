package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "chatmessage")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatmessage_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;
    
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
} 