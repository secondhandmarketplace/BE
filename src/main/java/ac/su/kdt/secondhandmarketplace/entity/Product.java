package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@Table(name = "product")
@NoArgsConstructor // 파라미터 없는 기본 생성자를 자동으로 생성. JPA 사용 시 필수적
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long id;  // 상품 고유 식별자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id2", nullable = false)
    private Category category;  // 상품 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 판매자 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_predicted_category_id")
    private Category aiPredictedCategory;  // AI가 예측한 카테고리
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;  // 상품명 (100자 제한)
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;  // 상품 상세 설명

    @Column(name = "price", precision = 10, scale = 0)
    private BigDecimal price;  // 상품 가격

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;  // 상품 상태 (판매중, 예약중 등)

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>(); // 상품 이미지 목록

    @PrePersist // INSERT 쿼리가 실행되기 전에 실행됩니다.
    protected void onCreate() {
        if (createAt == null) { // createAt이 null일 경우에만 현재 시간으로 설정
            createAt = LocalDateTime.now(); // 현재 시간으로 createAt 필드를 초기화
        }
        if (updateAt == null) { // updateAt이 null일 경우에만 현재 시간으로 설정
            updateAt = LocalDateTime.now(); // 현재 시간으로 updateAt 필드를 초기화
        }
        if (viewCount == null) { // viewCount가 null일 경우 0으로 초기화
            viewCount = 0;
        }
        if (chatCount == null) { // chatCount가 null일 경우 0으로 초기화
            chatCount = 0;
        }
    }

    // 업데이트 시간 자동 설정
    @PreUpdate // UPDATE 쿼리가 실행되기 전에 실행됩니다.
    protected void onUpdate() {
        updateAt = LocalDateTime.now(); // 업데이트 시마다 updateAt 필드를 현재 시간으로 갱신합니다.
    }

    // 편의 메서드: 상품에 이미지 추가
    public void addImage(ProductImage image) {
        images.add(image); // 상품 이미지 리스트에 이미지를 추가합니다.
        image.setProduct(this); // ProductImage 엔티티의 product 필드를 현재 Product 엔티티로 설정하여 양방향 연관관계를 맺습니다.
    }

    // 편의 메서드: 상품에서 이미지 제거
    public void removeImage(ProductImage image) {
        images.remove(image); // 상품 이미지 리스트에서 이미지를 제거합니다.
        image.setProduct(null); // ProductImage 엔티티의 product 필드를 null로 설정하여 연관관계를 끊습니다.
    }
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms;  // 상품 관련 채팅방 목록
}