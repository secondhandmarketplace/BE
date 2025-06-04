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
@Table(name = "productImage")
@NoArgsConstructor // 파라미터 없는 기본 생성자를 자동으로 생성. JPA 사용 시 필수적
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "image_url", nullable = false, length = 200)
    private String imageUrl;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence; // 이미지 순서
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    // 업로드 시간 자동 설정
    @PrePersist // INSERT 쿼리가 실행되기 전에 실행
    protected void onCreate() {
        if (uploadedAt == null) { // uploadedAt이 null일 경우에만 현재 시간으로 설정
            uploadedAt = LocalDateTime.now(); // 현재 시간으로 uploadedAt 필드를 초기화
        }
    }
} 