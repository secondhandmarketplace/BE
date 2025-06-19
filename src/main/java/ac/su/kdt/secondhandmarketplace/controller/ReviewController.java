package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.dto.transaction.ReviewCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.transaction.ReviewResponse;
import ac.su.kdt.secondhandmarketplace.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService; // ReviewService 주입

    // 새로운 거래 후기를 등록합니다.
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    // 특정 ID의 리뷰를 조회합니다.
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    // 특정 거래에 연결된 리뷰를 조회합니다.
    @GetMapping("/by-transaction/{transactionId}")
    public ResponseEntity<ReviewResponse> getReviewByTransactionId(@PathVariable Long transactionId) {
        ReviewResponse review = reviewService.getReviewByTransactionId(transactionId);
        return ResponseEntity.ok(review);
    }

    // 특정 리뷰 작성자의 리뷰 목록을 조회합니다.
    @GetMapping("/by-reviewer/{reviewerId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByReviewer(
            @PathVariable Long reviewerId,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByReviewer(reviewerId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 특정 상품에 대한 리뷰 목록을 조회합니다.
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 특정 리뷰의 내용을 수정합니다.
    @PutMapping("/{reviewId}/content")
    public ResponseEntity<ReviewResponse> updateReviewContent(
            @PathVariable Long reviewId,
            @RequestBody String content) {
        ReviewResponse updatedReview = reviewService.updateReviewContent(reviewId, content);
        return ResponseEntity.ok(updatedReview);
    }

    // 특정 리뷰를 삭제합니다. (실제 삭제)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}