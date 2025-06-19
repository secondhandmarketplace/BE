package ac.su.kdt.secondhandmarketplace.dto.transaction;

import ac.su.kdt.secondhandmarketplace.entity.Review; // Review 엔티티 임포트
//import ac.su.kdt.secondhandmarketplace.user.UserResponse; // UserResponse DTO 임포트

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long reviewId; // 리뷰 고유 식별자
    private Long transactionId; // 관련 거래 ID
    private Long reviewerId; // 리뷰 작성자 ID
    private String reviewerUsername; // 리뷰 작성자명
    private Integer rating; // 평점
    private String content; // 리뷰 내용
    private LocalDateTime createAt; // 리뷰 작성 시간

    // Review 엔티티를 ReviewResponse DTO로 변환하는 정적 팩토리 메서드
    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getId()); // 리뷰 ID 설정
        response.setTransactionId(review.getTransaction() != null ? review.getTransaction().getId() : null); // 거래 ID 설정
        response.setReviewerId(review.getReviewer() != null ? review.getReviewer().getId() : null); // 작성자 ID 설정
        response.setReviewerUsername(review.getReviewer() != null ? review.getReviewer().getUsername() : null); // 작성자 이름 설정
        response.setRating(review.getRating()); // 평점 설정
        response.setContent(review.getContent()); // 내용 설정
        response.setCreateAt(review.getCreateAt()); // 생성 시간 설정
        return response; // 변환된 ReviewResponse DTO 반환
    }
}