package com.kdt.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore  // JSON 직렬화에서 제외하여 무한 참조 방지
    @JoinColumn(name = "seller_userid", referencedColumnName = "userid")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore  // JSON 직렬화에서 제외하여 무한 참조 방지
    @JoinColumn(name = "buyer_userid", referencedColumnName = "userid")
    private User buyer;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "meet_location", length = 255)
    private String meetLocation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.판매중;

    @Column(name = "reg_date", nullable = false)
    private LocalDateTime regDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(length = 500)
    private String thumbnail;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "ai_price_min", precision = 15, scale = 2)
    private BigDecimal aiPriceMin;

    @Column(name = "ai_price_max", precision = 15, scale = 2)
    private BigDecimal aiPriceMax;

    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore  // JSON 직렬화에서 제외하여 무한 참조 방지
    private List<ItemImage> itemImages = new ArrayList<>();

    // === 비즈니스 메서드 ===

    /**
     * 아이템 이미지 추가
     */
    public void addItemImage(ItemImage image) {
        if (image != null) {
            this.itemImages.add(image);
            image.setItem(this);
        }
    }

    /**
     * 아이템 이미지 제거
     */
    public void removeItemImage(ItemImage image) {
        if (image != null) {
            this.itemImages.remove(image);
            image.setItem(null);
        }
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 판매자 ID 반환 (JSON 직렬화용)
     */
    public String getSellerId() {
        return this.seller != null ? this.seller.getUserid() : null;
    }

    /**
     * 구매자 ID 반환 (JSON 직렬화용)
     */
    public String getBuyerId() {
        return this.buyer != null ? this.buyer.getUserid() : null;
    }

    /**
     * 첫 번째 이미지 경로 반환
     */
    public String getFirstImagePath() {
        return this.itemImages != null && !this.itemImages.isEmpty()
                ? this.itemImages.get(0).getPhotoPath()
                : null;
    }

    /**
     * 상태 변경 (거래 완료 시 완료 날짜 자동 설정)
     */
    public void changeStatus(Status newStatus) {
        this.status = newStatus;
        if (newStatus == Status.거래완료) {
            this.completedDate = LocalDateTime.now();
        }
    }

    /**
     * 등록일 자동 설정 (생성 시)
     */
    @PrePersist
    protected void onCreate() {
        if (this.regDate == null) {
            this.regDate = LocalDateTime.now();
        }
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
    }

    // === Enum 정의 ===

    public enum Status {
        판매중("판매중"),
        예약중("예약중"),
        거래완료("거래완료");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 문자열로부터 Status 변환
         */
        public static Status fromString(String status) {
            if (status == null) return 판매중;

            for (Status s : Status.values()) {
                if (s.name().equals(status) || s.description.equals(status)) {
                    return s;
                }
            }
            return 판매중; // 기본값
        }
    }

    // === equals & hashCode (ID 기반) ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return itemid != null && itemid.equals(item.itemid);
    }

    @Override
    public int hashCode() {
        return itemid != null ? itemid.hashCode() : 0;
    }

    // === toString (순환 참조 방지) ===

    @Override
    public String toString() {
        return "Item{" +
                "itemid=" + itemid +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", regDate=" + regDate +
                ", sellerId='" + getSellerId() + '\'' +
                '}';
    }
}
