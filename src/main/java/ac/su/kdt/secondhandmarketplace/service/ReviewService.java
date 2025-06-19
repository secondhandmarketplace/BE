package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.transaction.ReviewCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.transaction.ReviewResponse;
import ac.su.kdt.secondhandmarketplace.entity.Review;
import ac.su.kdt.secondhandmarketplace.entity.Transaction;
import ac.su.kdt.secondhandmarketplace.entity.User;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import ac.su.kdt.secondhandmarketplace.repository.TransactionRepository;
import ac.su.kdt.secondhandmarketplace.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final로 선언된 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // 새로운 거래 후기를 등록
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        // 1. 거래 존재 여부 확인
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("거래 내역을 찾을 수 없습니다: " + request.getTransactionId()));

        // 2. 해당 거래에 이미 리뷰가 존재하는지 확인 (중복 리뷰 방지)
        if (reviewRepository.existsByTransaction_Id(request.getTransactionId())) {
            throw new IllegalStateException("해당 거래에 대한 리뷰가 이미 존재합니다.");
        }

        // 3. 리뷰 작성자 존재 여부 확인
        User reviewer = userRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new EntityNotFoundException("리뷰 작성자를 찾을 수 없습니다: " + request.getReviewerId()));

        // 4. 리뷰 작성자가 해당 거래의 구매자인지 확인
        // (필요에 따라 판매자도 리뷰를 남길 수 있도록 허용하거나, 특정 역할만 가능하도록 비즈니스 로직 정의)
        if (!transaction.getBuyer().getId().equals(reviewer.getId())) {
            throw new IllegalArgumentException("리뷰는 해당 거래의 구매자만 작성할 수 있습니다.");
        }

        // 5. Review 엔티티 생성 및 초기화
        Review review = new Review();
        review.setTransaction(transaction); // 관련 거래 설정
        review.setReviewer(reviewer);     // 리뷰 작성자 설정
        review.setRating(request.getRating()); // 평점 설정
        review.setContent(request.getContent()); // 내용 설정
        review.setCreateAt(LocalDateTime.now()); // 작성 시간 설정

        // 6. 리뷰를 데이터베이스에 저장
        Review savedReview = reviewRepository.save(review);

        // 7. Transaction 엔티티에 review 연결 (양방향 매핑을 위해)
        // DDL 상 review_id2가 Transaction 테이블에 있으므로, 이 부분은 Review를 저장한 후
        // Transaction을 업데이트하여 review_id2 필드를 채워야 합니다.
        transaction.setReview(savedReview); // Transaction에 Review 객체 연결
        transactionRepository.save(transaction); // 변경된 Transaction 저장

        // TODO: 매너 점수 업데이트 로직 추가 (리뷰 평점을 기반으로 구매자/판매자의 매너 점수 업데이트)
        // User reviewerUser = savedReview.getReviewer();
        // int currentMannerScore = reviewerUser.getMannerScore();
        // int newMannerScore = calculateNewMannerScore(currentMannerScore, savedReview.getRating());
        // reviewerUser.setMannerScore(newMannerScore);
        // userRepository.save(reviewerUser);

        // 8. 저장된 리뷰 엔티티를 응답 DTO로 변환하여 반환
        return ReviewResponse.fromEntity(savedReview);
    }

    // 특정 ID의 리뷰를 조회
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        // 리뷰 ID로 Review 엔티티를 찾습니다. 없으면 예외 발생
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));
        
        // 조회된 리뷰 엔티티를 응답 DTO로 변환하여 반환
        return ReviewResponse.fromEntity(review);
    }

    // 특정 거래에 연결된 리뷰를 조회
    @Transactional(readOnly = true)
    public ReviewResponse getReviewByTransactionId(Long transactionId) {
        // 거래 ID로 리뷰를 찾습니다. 없으면 예외 발생 (또는 null 반환, 정책에 따라 다름)
        Review review = reviewRepository.findByTransaction_Id(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 거래에 대한 리뷰를 찾을 수 없습니다: " + transactionId));
        return ReviewResponse.fromEntity(review);
    }

    // 특정 리뷰 작성자의 리뷰 목록을 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByReviewer(Long reviewerId, Pageable pageable) {
        // 리뷰 작성자 ID로 리뷰 목록을 페이징하여 조회하고, ReviewResponse DTO로 변환
        return reviewRepository.findByReviewer_Id(reviewerId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    // 특정 상품에 대한 리뷰 목록을 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        // 상품 ID로 리뷰 목록을 페이징하여 조회하고, ReviewResponse DTO로 변환
        return reviewRepository.findByTransaction_Product_Id(productId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    // 특정 리뷰의 내용을 수정 (평점은 수정 불가로 가정)

    @Transactional
    public ReviewResponse updateReviewContent(Long reviewId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));
        
        review.setContent(content); // 리뷰 내용 업데이트
        Review updatedReview = reviewRepository.save(review); // 변경사항 저장
        return ReviewResponse.fromEntity(updatedReview);
    }

    // 특정 리뷰를 삭제 (실제 삭제)

    @Transactional
    public void deleteReview(Long reviewId) {
        // 리뷰 존재 여부 확인 후 삭제
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("삭제할 리뷰를 찾을 수 없습니다: " + reviewId);
        }
        
        // 연결된 transaction의 review_id2 필드를 null로 설정하여 참조를 끊은 후 삭제해야 함
        // (단방향 @JoinColumn(review_id2)이 Transaction에 있어 Review 삭제 시 직접 관여 어려움)
        // 따라서 Review 삭제 전에 Transaction에서 해당 reviewId를 가진 review_id2를 null로 만들어야 합니다.
        // 이 부분을 자동으로 처리하려면 Transaction 엔티티의 review 필드에 @OneToOne(orphanRemoval = true)를 고려하거나
        // TransactionService에서 해당 Transaction을 먼저 업데이트해야 합니다.
        
        // 간편하게 직접 외래키를 끊는 로직을 추가 (정확한 구현은 비즈니스 로직에 따라 달라질 수 있음)
        Review reviewToDelete = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 리뷰를 찾을 수 없습니다: " + reviewId));
        
        // Transaction에서 Review 참조 끊기
        Transaction transaction = reviewToDelete.getTransaction();
        if (transaction != null) {
            transaction.setReview(null); // Review 엔티티와의 연결 끊기
            transactionRepository.save(transaction); // Transaction 업데이트
        }
        
        reviewRepository.deleteById(reviewId); // 리뷰 삭제
    }
}