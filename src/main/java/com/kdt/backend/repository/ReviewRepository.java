package com.kdt.backend.repository;

import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemTransaction;
import com.kdt.backend.entity.Review;
import com.kdt.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. 특정 아이템과 구매자로 리뷰가 이미 있는지 확인 (중복 리뷰 방지)
    boolean existsByItemAndBuyer(Item item, User buyer);

    // 2. 판매자(리뷰이) ID로 받은 후기들 조회
    List<Review> findByReviewee_Userid(String sellerId);

    // 3. 평균 별점 (판매자/리뷰이 기준)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee = :reviewee")
    Double findAverageRatingByReviewee(@Param("reviewee") User reviewee);

    // 4. 리뷰이(판매자) 기준 후기 개수
    int countByReviewee(User reviewee);

    // 5. 리뷰 작성자(구매자) 기준 후기 개수
    int countByReviewer(User reviewer);

    // 6. 특정 아이템에 대한 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item = :item")
    Double findAverageRatingByItem(@Param("item") User item);

    // 7. 특정 거래에 대한 리뷰 목록
    List<Review> findByTransaction(ItemTransaction transaction);

    // 8. 특정 판매자(리뷰이)에 대한 리뷰 목록
    List<Review> findByReviewee(User reviewee);

    // 9. 특정 아이템에 대한 리뷰 목록
    List<Review> findByItem(Item item);

    // 10. 특정 구매자(리뷰어) ID로 작성한 후기 조회
    List<Review> findByReviewer_Userid(String buyerId);

}
