package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user")
@NoArgsConstructor // 파라미터 없는 기본 생성자를 자동으로 생성. JPA 사용 시 필수적
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;  // 사용자 고유 식별자
    
    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;  // 소속 대학교
    
    @Column(nullable = false, length = 30)
    private String username;  // 로그인 아이디 (30자 제한)
    
    @Column(nullable = false, length = 100)
    private String password;  // 암호화된 비밀번호 (100자 제한)
    
    @Column(nullable = false, length = 100)
    private String email;  // 이메일 주소 (100자 제한)
    
    @Column(nullable = false, length = 30)
    private String nickname;  // 닉네임 (30자 제한)
    
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;  // 이메일 인증 여부
    
    @Column(name = "profile_image_url", length = 200)
    private String profileImageUrl;  // 프로필 이미지 URL (200자 제한)
    
    @Column(name = "manner_score", nullable = false)
    private Double mannerScore;  // 매너 점수
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;  // 계정 생성 시간
} 