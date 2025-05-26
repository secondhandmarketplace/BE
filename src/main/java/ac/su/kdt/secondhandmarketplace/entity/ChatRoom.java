package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "chatroom")
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id")
    private Long id;  // 채팅방 고유 식별자
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;  // 관련 상품
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 첫 번째 사용자
    
    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;  // 두 번째 사용자
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;  // 채팅방 생성 시간
    
    @Column(name = "buyer_unread_count", nullable = false)
    private Integer buyerUnreadCount;  // 구매자 미읽음 메시지 수
    
    @Column(name = "seller_unread_count", nullable = false)
    private Integer sellerUnreadCount;  // 판매자 미읽음 메시지 수
    
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;  // 채팅 메시지 목록
} 