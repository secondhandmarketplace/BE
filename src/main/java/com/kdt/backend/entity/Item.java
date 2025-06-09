package com.kdt.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    @JoinColumn(name = "seller_userid", referencedColumnName = "userid", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
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

    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ItemImage> itemImages = new ArrayList<>();

    @Column(name = "item_condition", length = 10)
    private String itemCondition;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tagsString;

    // === 비즈니스 메서드 ===

    public void addItemImage(ItemImage image) {
        if (image != null) {
            this.itemImages.add(image);
            image.setItem(this);
        }
    }

    public String getSellerId() {
        if (this.seller == null) {
            throw new IllegalStateException("판매자 정보가 누락되었습니다. itemId: " + this.itemid);
        }
        return this.seller.getUserid();
    }

    public String getSellerName() {
        if (this.seller == null) {
            throw new IllegalStateException("판매자 정보가 누락되었습니다. itemId: " + this.itemid);
        }
        return this.seller.getName();
    }

    public List<String> getTags() {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(tagsString.split(","));
    }

    public void setTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tagsString = null;
        } else {
            this.tagsString = String.join(",", tags);
        }
    }

    public String getFirstImagePath() {
        return this.itemImages != null && !this.itemImages.isEmpty()
                ? this.itemImages.get(0).getPhotoPath()
                : null;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void changeStatus(Status newStatus) {
        this.status = newStatus;
        if (newStatus == Status.거래완료) {
            this.completedDate = LocalDateTime.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.regDate == null) {
            this.regDate = LocalDateTime.now();
        }
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.seller == null) {
            throw new IllegalStateException("상품 등록 시 판매자 정보는 필수입니다.");
        }
    }

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

        public static Status fromString(String status) {
            if (status == null) return 판매중;
            for (Status s : Status.values()) {
                if (s.name().equals(status) || s.description.equals(status)) {
                    return s;
                }
            }
            return 판매중;
        }
    }
}
