package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Review;
import ac.su.kdt.secondhandmarketplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 특정 사용자의 리뷰 수 조회
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user = :user")
    Long countByUser(@Param("user") User user);
    
    // 특정 상품의 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // 특정 상품의 리뷰 목록 조회
    @Query("SELECT r FROM Review r JOIN r.transaction t WHERE t.product.id = :productId")
    List<Review> findByProductId(@Param("productId") Long productId);
} 