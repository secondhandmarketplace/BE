package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Review;
import ac.su.kdt.secondhandmarketplace.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 거래(transaction_id)에 해당하는 리뷰를 조회
    Optional<Review> findByTransaction_Id(Long Id);

    // 특정 리뷰 작성자(user_id)에 해당하는 리뷰 목록을 페이징하여 조회
    Page<Review> findByReviewer_Id(Long Id, Pageable pageable);

    // 특정 상품(product_id)에 해당하는 리뷰 목록을 페이징하여 조회
    // (Transaction을 통해 Product에 간접적으로 연결되므로, 쿼리 메서드 이름에 유의)
    Page<Review> findByTransaction_Product_Id(Long Id, Pageable pageable);

    // 특정 거래 ID에 해당하는 리뷰가 이미 존재하는지 확인 (중복 리뷰 방지)
    boolean existsByTransaction_Id(Long Id);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.transaction.product.id = :productId")
    Double getAverageRatingByProductId(Long productId);

    long countByReviewer(User user);
}