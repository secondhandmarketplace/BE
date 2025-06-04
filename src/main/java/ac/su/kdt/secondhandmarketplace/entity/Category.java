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
@Table(name = "category")
@NoArgsConstructor // 파라미터 없는 기본 생성자를 자동으로 생성. JPA 사용 시 필수적
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성

public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계를 나타냅니다. (여러 카테고리가 하나의 상위 카테고리를 가질 수 있음)
                                       // FetchType.LAZY는 상위 카테고리 정보가 필요할 때만 로드하도록 지연 로딩을 설정합니다.
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
    // 생성 시간 자동 설정
    @PrePersist // INSERT 쿼리가 실행되기 전에 실행
    protected void onCreate() {
        if (createAt == null) { // createAt이 null일 경우에만 현재 시간으로 설정
            createAt = LocalDateTime.now(); // 현재 시간으로 createAt 필드를 초기화
        }
    }
}