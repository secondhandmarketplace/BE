package com.kdt.backend.repository;

import com.kdt.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 카테고리와 상태로 조회
    List<Item> findByCategoryAndStatus(String category, Item.Status status);

    // 판매중인 상품 중 가격이 0보다 큰 것들만 조회하여 가격 오름차순 정렬
    List<Item> findByStatusAndPriceGreaterThanOrderByPriceAsc(Item.Status status, Integer price);

    // 가격 범위별 조회
    List<Item> findByStatusAndPriceBetweenOrderByPriceAsc(Item.Status status, Integer minPrice, Integer maxPrice);

    // 카테고리별 최저가 상품
    List<Item> findByCategoryAndStatusOrderByPriceAsc(String category, Item.Status status);

    // 전체 상품 중 최저가 (JPQL 쿼리 사용)
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.price > 0 ORDER BY i.price ASC")
    List<Item> findCheapestItems(@Param("status") Item.Status status);

    // 연관 상품 조회 (카테고리 같고, 특정 ID 제외, 상태별)
    List<Item> findByCategoryAndItemidNotAndStatusOrderByRegDateDesc(
            String category, Long excludeId, Item.Status status);

    // 전체 상품 + 판매자 + 이미지
    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN FETCH i.seller " +
            "LEFT JOIN FETCH i.itemImages " +
            "ORDER BY i.regDate DESC")
    List<Item> findAllWithSellerAndImages();

    // 상품 상세 (판매자+이미지)
    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.seller " +
            "LEFT JOIN FETCH i.itemImages " +
            "WHERE i.itemid = :id")
    Optional<Item> findItemWithSellerAndImagesById(@Param("id") Long id);

    // 판매자 기준 상품 전체
    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN FETCH i.seller s " +
            "LEFT JOIN FETCH i.itemImages " +
            "WHERE s.userid = :userId " +
            "ORDER BY i.regDate DESC")
    List<Item> findBySellerUserIdWithImages(@Param("userId") String userId);

    // 구매자 기준 거래완료 상품
    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN FETCH i.seller " +
            "LEFT JOIN FETCH i.itemImages " +
            "WHERE i.buyer.userid = :buyerId AND i.status = :status " +
            "ORDER BY i.regDate DESC")
    List<Item> findCompletedByBuyerUserId(@Param("buyerId") String buyerId, @Param("status") Item.Status status);

    // 키워드 검색 (제목/설명)
    @Query("SELECT DISTINCT i FROM Item i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.regDate DESC")
    List<Item> findTop10ByKeyword(@Param("keyword") String keyword);

    // 상품 상세 (이미지만)
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemImages WHERE i.itemid = :id")
    Optional<Item> findItemWithImagesById(@Param("id") Long id);

    // 전체 상품 (이미지만)
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemImages ORDER BY i.regDate DESC")
    List<Item> findAllWithImages();

    // 카테고리+제목 부분일치 (Spring Data JPA 네이밍 규칙)
    List<Item> findByCategoryAndTitleContaining(String category, String title);

    // 카테고리+제목 부분일치, 최근 등록순 정렬
    List<Item> findByCategoryAndTitleContainingOrderByRegDateDesc(String category, String title);

    // 상품 상태별 조회
    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.itemCondition = :condition ORDER BY i.regDate DESC")
    List<Item> findByCategoryAndCondition(@Param("category") String category, @Param("condition") String condition);

    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.itemCondition = :condition AND i.title LIKE %:title% ORDER BY i.regDate DESC")
    List<Item> findByCategoryAndConditionAndTitleContaining(
            @Param("category") String category,
            @Param("condition") String condition,
            @Param("title") String title
    );

    List<Item> findByStatusOrderByPriceDesc(Item.Status status);
    List<Item> findByStatus(Item.Status status);

    // 최근 등록순 정렬 메서드들
    List<Item> findAllByOrderByRegDateDesc();
    List<Item> findByStatusOrderByRegDateDesc(Item.Status status);
    List<Item> findByCategoryAndStatusOrderByRegDateDesc(String category, Item.Status status);
    List<Item> findByStatusOrderByViewCountDesc(Item.Status status);

    @Query("SELECT i FROM Item i WHERE i.status = :status AND (i.title LIKE %:keyword% OR i.description LIKE %:keyword%) ORDER BY i.regDate DESC")
    List<Item> findByKeywordOrderByRegDateDesc(@Param("keyword") String keyword, @Param("status") Item.Status status);

    @Query("SELECT i FROM Item i WHERE i.seller.userid = :sellerId ORDER BY i.regDate DESC")
    List<Item> findBySellerUseridOrderByRegDateDesc(@Param("sellerId") String sellerId);

    List<Item> findByStatusAndPriceBetweenOrderByRegDateDesc(Item.Status status, Integer minPrice, Integer maxPrice);

    @Query("SELECT DISTINCT i FROM Item i WHERE i.category = :category AND i.status = :status ORDER BY i.regDate DESC")
    List<Item> findDistinctByCategoryAndStatusOrderByRegDateDesc(@Param("category") String category, @Param("status") Item.Status status);

    // 추가 메서드들 (가격 추천 시스템용, 최근 등록순)
    List<Item> findByTitleContainingOrderByRegDateDesc(String title);
    List<Item> findByCategoryOrderByRegDateDesc(String category);
    List<Item> findByStatusAndTitleContainingOrderByRegDateDesc(Item.Status status, String title);
    List<Item> findByCategoryAndStatusAndTitleContainingOrderByRegDateDesc(
            String category, Item.Status status, String title);

    // 상품 상태(itemCondition)별 조회 메서드들
    List<Item> findByItemConditionOrderByRegDateDesc(String itemCondition);
    List<Item> findByCategoryAndItemConditionOrderByRegDateDesc(String category, String itemCondition);
    List<Item> findByStatusAndItemConditionOrderByRegDateDesc(Item.Status status, String itemCondition);
    List<Item> findByCategoryAndStatusAndItemConditionOrderByRegDateDesc(
            String category, Item.Status status, String itemCondition);

    // 판매자/구매자별 상태별 조회
    List<Item> findBySellerUseridAndStatusOrderByRegDateDesc(String sellerId, Item.Status status);
    List<Item> findByBuyerUseridAndStatusOrderByRegDateDesc(String buyerId, Item.Status status);
}
