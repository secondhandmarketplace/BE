package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "review")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reviewer;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @PrePersist
    protected void onCreate() {
        if (createAt == null) { // createAt이 null일 경우 현재 시간으로 설정
            createAt = LocalDateTime.now(); // 현재 시간으로 createAt 초기화
        }
    }
} 