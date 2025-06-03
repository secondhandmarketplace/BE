package com.kdt.backend.dto;

import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemImage;
import com.kdt.backend.repository.ReviewRepository;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private Long id;                    // 통합: itemid, item id 모두 id로
    private String title;
    private String category;
    private String description;
    private BigDecimal price;
    private String status;
    private String locationInfo;
    private Integer viewCount;
    private BigDecimal aiPriceMin;
    private BigDecimal aiPriceMax;
    private Double mannerScore;
    private Double averageRating;
    private Integer reviewCount;
    private String sellerId;
    private String sellerName;
    private Integer sellerReviewCount;
    private String regdate;
    private List<String> itemImages;
    private String meetLocation;
    private String thumbnail;
    private String imageUrl;            // 대표 이미지
    private LocalDateTime regDate;      // 원본 날짜
    private LocalDateTime completedDate; // 거래 완료 날짜
    private String value;               // 상품 상태 (S, A, B, C, D)
    private List<String> tags;          // 태그 목록

    /**
     * Item → DTO 변환 (ReviewRepository 없이)
     * 기본적인 상품 정보만 포함
     */
    public static ItemResponseDTO from(Item item) {
        if (item == null) {
            return null;
        }

        return ItemResponseDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .category(item.getCategory())
                .description(item.getDescription())
                .price(item.getPrice() != null ? BigDecimal.valueOf(item.getPrice()) : BigDecimal.ZERO)
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .meetLocation(item.getMeetLocation())
                .locationInfo(item.getMeetLocation())
                .viewCount(item.getViewCount() != null ? item.getViewCount() : 0)
                .aiPriceMin(item.getAiPriceMin())
                .aiPriceMax(item.getAiPriceMax())
                .regDate(item.getRegDate())
                .regdate(item.getRegDate() != null ?
                        item.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")
                .completedDate(item.getCompletedDate())
                .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                .sellerName(item.getSeller() != null ? item.getSeller().getName() : "알 수 없음")
                .mannerScore(item.getSeller() != null && item.getSeller().getMannerScore() != null ?
                        item.getSeller().getMannerScore().doubleValue() : 0.0)
                .itemImages(item.getItemImages() != null ?
                        item.getItemImages().stream()
                                .map(ItemImage::getPhotoPath)
                                .collect(Collectors.toList()) : List.of())
                .imageUrl(item.getFirstImagePath()) // 첫 번째 이미지를 대표 이미지로
                .thumbnail(item.getThumbnail())
                // 리뷰 관련은 기본값
                .averageRating(0.0)
                .reviewCount(0)
                .sellerReviewCount(0)
                .build();
    }

    /**
     * Item → DTO 변환 (ReviewRepository 포함)
     * 리뷰 정보까지 포함한 완전한 정보
     */
    public static ItemResponseDTO from(Item item, ReviewRepository reviewRepository) {
        if (item == null) {
            return null;
        }

        ItemResponseDTO dto = ItemResponseDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .category(item.getCategory())
                .description(item.getDescription())
                .price(item.getPrice() != null ? BigDecimal.valueOf(item.getPrice()) : BigDecimal.ZERO)
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .meetLocation(item.getMeetLocation())
                .locationInfo(item.getMeetLocation())
                .viewCount(item.getViewCount() != null ? item.getViewCount() : 0)
                .aiPriceMin(item.getAiPriceMin())
                .aiPriceMax(item.getAiPriceMax())
                .regDate(item.getRegDate())
                .regdate(item.getRegDate() != null ?
                        item.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")
                .completedDate(item.getCompletedDate())
                .itemImages(item.getItemImages() != null ?
                        item.getItemImages().stream()
                                .map(ItemImage::getPhotoPath)
                                .collect(Collectors.toList()) : List.of())
                .imageUrl(item.getFirstImagePath())
                .thumbnail(item.getThumbnail())
                .build();

        // 판매자 정보 설정
        if (item.getSeller() != null) {
            dto.setSellerId(item.getSeller().getUserid());
            dto.setSellerName(item.getSeller().getName());
            dto.setMannerScore(item.getSeller().getMannerScore() != null ?
                    item.getSeller().getMannerScore().doubleValue() : 0.0);

            // 리뷰 정보 설정 (ReviewRepository 활용)
            if (reviewRepository != null) {
                try {
                    // 판매자에 대한 리뷰 수 (countByReviewee 사용)
                    int sellerReviewCount = reviewRepository.countByReviewee(item.getSeller());
                    dto.setSellerReviewCount(sellerReviewCount);
                    dto.setReviewCount(sellerReviewCount);

                    // 판매자 평균 평점 (findAverageRatingByReviewee 사용)
                    Double avgRating = reviewRepository.findAverageRatingByReviewee(item.getSeller());
                    dto.setAverageRating(avgRating != null ? avgRating : 0.0);

                } catch (Exception e) {
                    // ReviewRepository 메서드 호출 실패 시 기본값
                    dto.setSellerReviewCount(0);
                    dto.setAverageRating(0.0);
                    dto.setReviewCount(0);
                }
            } else {
                // ReviewRepository가 null인 경우 기본값
                dto.setSellerReviewCount(0);
                dto.setAverageRating(0.0);
                dto.setReviewCount(0);
            }
        } else {
            // 판매자 정보가 없는 경우
            dto.setSellerId(null);
            dto.setSellerName("알 수 없음");
            dto.setMannerScore(0.0);
            dto.setSellerReviewCount(0);
            dto.setAverageRating(0.0);
            dto.setReviewCount(0);
        }

        return dto;
    }

    /**
     * 간단한 정보만 포함한 DTO 생성 (목록용)
     */
    public static ItemResponseDTO fromSimple(Item item) {
        if (item == null) {
            return null;
        }

        return ItemResponseDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .price(item.getPrice() != null ? BigDecimal.valueOf(item.getPrice()) : BigDecimal.ZERO)
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .imageUrl(item.getFirstImagePath())
                .thumbnail(item.getThumbnail())
                .regDate(item.getRegDate())
                .viewCount(item.getViewCount() != null ? item.getViewCount() : 0)
                .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                .build();
    }

    /**
     * 검색 결과용 DTO 생성
     */
    public static ItemResponseDTO fromSearch(Item item) {
        if (item == null) {
            return null;
        }

        return ItemResponseDTO.builder()
                .id(item.getItemid())
                .title(item.getTitle())
                .category(item.getCategory())
                .price(item.getPrice() != null ? BigDecimal.valueOf(item.getPrice()) : BigDecimal.ZERO)
                .status(item.getStatus() != null ? item.getStatus().name() : "판매중")
                .imageUrl(item.getFirstImagePath())
                .locationInfo(item.getMeetLocation())
                .regDate(item.getRegDate())
                .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                .sellerName(item.getSeller() != null ? item.getSeller().getName() : "알 수 없음")
                .build();
    }

    /**
     * 리뷰 정보 포함 상세 DTO 생성 (상품 상세 페이지용)
     */
    public static ItemResponseDTO fromDetail(Item item, ReviewRepository reviewRepository) {
        ItemResponseDTO dto = from(item, reviewRepository);

        // 추가 상세 정보 설정
        if (dto != null && item != null) {
            // 모든 이미지 URL 설정
            if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                List<String> imageUrls = item.getItemImages().stream()
                        .map(ItemImage::getPhotoPath)
                        .collect(Collectors.toList());
                dto.setItemImages(imageUrls);
            }
        }

        return dto;
    }

    // === 유틸리티 메서드 ===

    /**
     * 대표 이미지 URL 반환
     */
    public String getMainImageUrl() {
        if (itemImages != null && !itemImages.isEmpty()) {
            return itemImages.get(0);
        }
        return thumbnail != null ? thumbnail : imageUrl;
    }

    /**
     * 가격을 문자열로 포맷팅
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "0원";
        }
        return String.format("%,d원", price.intValue());
    }

    /**
     * 등록일을 상대적 시간으로 표시
     */
    public String getRelativeRegDate() {
        if (regDate == null) {
            return "알 수 없음";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(regDate, now).toMinutes();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (minutes < 1440) { // 24시간
            return (minutes / 60) + "시간 전";
        } else {
            return (minutes / 1440) + "일 전";
        }
    }

    /**
     * 상품 상태가 판매중인지 확인
     */
    public boolean isAvailable() {
        return "판매중".equals(status);
    }

    /**
     * 상품 상태가 거래완료인지 확인
     */
    public boolean isCompleted() {
        return "거래완료".equals(status);
    }

    /**
     * 평점을 별점 문자열로 변환
     */
    public String getStarRating() {
        if (averageRating == null || averageRating == 0.0) {
            return "☆☆☆☆☆";
        }

        int fullStars = averageRating.intValue();
        boolean hasHalfStar = (averageRating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("☆");
        }
        while (stars.length() < 5) {
            stars.append("☆");
        }

        return stars.toString();
    }

    /**
     * 판매자 신뢰도 점수 계산
     */
    public String getTrustScore() {
        if (mannerScore == null || averageRating == null) {
            return "신규";
        }

        double score = (mannerScore + averageRating) / 2.0;
        if (score >= 4.5) {
            return "우수";
        } else if (score >= 3.5) {
            return "좋음";
        } else if (score >= 2.5) {
            return "보통";
        } else {
            return "주의";
        }
    }
}
