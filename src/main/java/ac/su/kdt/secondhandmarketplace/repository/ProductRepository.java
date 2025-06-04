package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 제목과 설명에 특정 키워드가 포함된 상품을 검색하고, 상태가 'FOR_SALE'인 상품만 조회
    // 대소문자 구분 없이 검색하며, Pageable을 사용하여 페이징 및 정렬을 지원
    Page<Product> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
            String titleKeyword, String descriptionKeyword, ProductStatus status, Pageable pageable);

    // 주어진 카테고리 ID에 해당하는 상품들을 상태와 함께 페이징하여 조회
    Page<Product> findByCategory_IdAndStatus(Long Id, ProductStatus status, Pageable pageable);

    // 주어진 사용자 ID(판매자 ID)에 해당하는 상품들을 상태와 함께 페이징하여 조회
    Page<Product> findByUser_IdAndStatus(Long Id, ProductStatus status, Pageable pageable);

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
    List<Product> findProductsByCriteria(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("location") String location,
            @Param("category") String category,
            @Param("minMannerScore") Double minMannerScore,
            @Param("minRating") Double minRating,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );



}