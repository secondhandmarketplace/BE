package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;  // 상품 고유 식별자
    
    @ManyToOne
    @JoinColumn(name = "category_id2", nullable = false)
    private Category category;  // 상품 카테고리
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 판매자 정보
    
    @ManyToOne
    @JoinColumn(name = "ai_predicted_category_id")
    private Category aiPredictedCategory;  // AI가 예측한 카테고리
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;  // 상품명 (100자 제한)
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;  // 상품 상세 설명

    @Column(name = "price", precision = 10, scale = 0)
    private BigDecimal price;  // 상품 가격

    @Column(name = "status", nullable = false, length = 20)
    private String status;  // 상품 상태 (판매중, 예약중 등)

    @Column(name = "ai_price_min", precision = 10, scale = 0)
    private BigDecimal aiPriceMin;  // AI 예측 최소 가격

    @Column(name = "ai_price_max", precision = 10, scale = 0)
    private BigDecimal aiPriceMax;  // AI 예측 최대 가격

    @Column(name = "view_count")
    private Integer viewCount;  // 조회수

    @Column(name = "chat_count")
    private Integer chatCount;  // 채팅 수
    
    @Column(name = "location_info", length = 100)
    private String locationInfo;  // 위치 정보 (100자 제한)
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;  // 상품 등록 시간
    
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;  // 상품 정보 수정 시간
    
    @Column(name = "refreshed_at")
    private LocalDateTime refreshedAt;  // 상품 새로고침 시간
    
    @Column(name = "sold_at")
    private LocalDateTime soldAt;  // 판매 완료 시간
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;  // 상품 이미지 목록
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms;  // 상품 관련 채팅방 목록
} 