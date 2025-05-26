package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "useractivitylog")
public class UserActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;
    
    @Column(name = "activity_type", nullable = false, length = 30)
    private String activityType;
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "activity_detail", columnDefinition = "TEXT")
    private String activityDetail;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
} 