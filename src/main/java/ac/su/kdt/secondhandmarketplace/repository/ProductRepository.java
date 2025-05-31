package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT p FROM Product p 
    WHERE 
        (:productName IS NULL OR p.title LIKE CONCAT('%', :productName, '%')) AND 
        (:category IS NULL OR p.category.categoryName = :category) AND 
        (:maxPrice IS NULL OR p.price <= :maxPrice) AND 
        (:minPrice IS NULL OR p.price >= :minPrice) AND 
        (:location IS NULL OR p.locationInfo LIKE CONCAT('%', :location, '%')) AND 
        p.status = '판매중' AND 
        (:minMannerScore IS NULL OR p.user.mannerScore >= :minMannerScore) 
    ORDER BY 
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'asc' THEN p.price END ASC, 
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'desc' THEN p.price END DESC, 
        CASE WHEN :sortBy = 'rating' AND :sortDirection = 'asc' THEN 
            (SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product = p) 
        END ASC, 
        CASE WHEN :sortBy = 'rating' AND :sortDirection = 'desc' THEN 
            (SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product = p) 
        END DESC, 
        CASE WHEN :sortBy = 'viewCount' AND :sortDirection = 'asc' THEN p.viewCount END ASC, 
        CASE WHEN :sortBy = 'viewCount' AND :sortDirection = 'desc' THEN p.viewCount END DESC, 
        CASE WHEN :sortBy = 'chatCount' AND :sortDirection = 'asc' THEN 
            (SELECT COUNT(c) FROM p.chatRooms c) 
        END ASC, 
        CASE WHEN :sortBy = 'chatCount' AND :sortDirection = 'desc' THEN 
            (SELECT COUNT(c) FROM p.chatRooms c) 
        END DESC 
""")

    List<Product> findByRecommendationCriteria( // 상품 추천 조건에 맞는 상품을 조회하는 메서드
        @Param("productName") String productName,
        @Param("category") String category,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minPrice") BigDecimal minPrice,
        @Param("location") String location,
        @Param("minMannerScore") Double minMannerScore,
        @Param("sortBy") String sortBy,
        @Param("sortDirection") String sortDirection
    );


    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.user u
    LEFT JOIN FETCH p.category c
    WHERE p.status = '판매중'
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:location IS NULL OR p.locationInfo LIKE CONCAT('%', :location, '%'))
        AND (:category IS NULL OR c.categoryName = :category)
        AND (:minMannerScore IS NULL OR u.mannerScore >= :minMannerScore)
        AND (:minRating IS NULL OR 
            (SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product = p) >= :minRating)
    ORDER BY 
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'asc' THEN p.price END ASC,
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'desc' THEN p.price END DESC,
        CASE WHEN :sortBy = 'rating' AND :sortDirection = 'asc' THEN 
            (SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product = p) 
        END ASC,
        CASE WHEN :sortBy = 'rating' AND :sortDirection = 'desc' THEN 
            (SELECT AVG(r.rating) FROM Review r JOIN r.transaction t WHERE t.product = p) 
        END DESC,
        CASE WHEN :sortBy = 'viewCount' AND :sortDirection = 'asc' THEN p.viewCount END ASC,
        CASE WHEN :sortBy = 'viewCount' AND :sortDirection = 'desc' THEN p.viewCount END DESC,
        CASE WHEN :sortBy = 'chatCount' AND :sortDirection = 'asc' THEN 
            (SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.product = p) 
        END ASC,
        CASE WHEN :sortBy = 'chatCount' AND :sortDirection = 'desc' THEN 
            (SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.product = p) 
        END DESC
""")
    List<Product> findProductsByCriteria( // 사용자가 말한 검색 조건에 맞는 상품을 데이터 베이스에서 조회하기위한 메서드
            @Param("minPrice") BigDecimal minPrice, // 최소 가격
            @Param("maxPrice") BigDecimal maxPrice, // 최대 가격
            @Param("location") String location, // 지역 정보
            @Param("category") String category, // 카테고리
            @Param("minMannerScore") Double minMannerScore, // 최소 매너 점수
            @Param("minRating") Double minRating, // 최소 평점
            @Param("sortBy") String sortBy, // 정렬 기준 (price, rating, viewCount, chatCount)
            @Param("sortDirection") String sortDirection // 정렬 방향 (asc, desc)
    );

    /**
     * 카테고리와 제품명으로 유사 제품을 검색
     */
    @Query("SELECT p FROM Product p WHERE p.category.categoryName = :category AND p.title LIKE %:keyword%")
    List<Product> findByCategoryAndTitleContaining(
        @Param("category") String category,
        @Param("keyword") String keyword
    );

    /**
     * 카테고리와 상품 상태로 유사 제품을 검색
     */
    @Query("SELECT p FROM Product p WHERE p.category.categoryName = :category AND p.status = :condition")
    List<Product> findByCategoryAndCondition(
        @Param("category") String category,
        @Param("condition") String condition
    );

}