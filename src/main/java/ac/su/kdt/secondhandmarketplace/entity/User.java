package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;
    
    @Column(nullable = false, length = 30)
    private String username;
    
    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 30)
    private String nickname;
    
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;
    
    @Column(name = "profile_image_url", length = 200)
    private String profileImageUrl;
    
    @Column(name = "manner_score", nullable = false)
    private Integer mannerScore;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
} 