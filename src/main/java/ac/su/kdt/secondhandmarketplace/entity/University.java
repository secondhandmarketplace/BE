package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "university")
@NoArgsConstructor // 파라미터 없는 기본 생성자를 자동으로 생성. JPA 사용 시 필수적
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성
public class University {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id", nullable = false)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "email_domain", nullable = false, length = 50)
    private String emailDomain;
} 