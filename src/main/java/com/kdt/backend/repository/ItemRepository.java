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

    // 연관 상품 조회 (카테고리 같고, 특정 ID 제외, 상태별)
    List<Item> findByCategoryAndItemidNotAndStatusOrderByRegDateDesc(
            String category, Long excludeId, Item.Status status);

    // 전체 상품 + 판매자 + 이미지
    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN FETCH i.seller " +
            "LEFT JOIN FETCH i.itemImages")
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
            "WHERE s.userid = :userId")
    List<Item> findBySellerUserIdWithImages(@Param("userId") String userId);

    // 구매자 기준 거래완료 상품
    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN FETCH i.seller " +
            "LEFT JOIN FETCH i.itemImages " +
            "WHERE i.buyer.userid = :buyerId AND i.status = '거래완료'")
    List<Item> findCompletedByBuyerUserId(@Param("buyerId") String buyerId);

    // 키워드 검색 (제목/설명)
    @Query("SELECT DISTINCT i FROM Item i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY i.regDate DESC")
    List<Item> findTop10ByKeyword(@Param("keyword") String keyword);

    // 상품 상세 (이미지만)
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemImages WHERE i.itemid = :id")
    Optional<Item> findItemWithImagesById(@Param("id") Long id);

    // 전체 상품 (이미지만)
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemImages ORDER BY i.regDate DESC")
    List<Item> findAllWithImages();

    // ✅ 카테고리+제목 부분일치 (Spring Data JPA 네이밍 규칙)
    List<Item> findByCategoryAndTitleContaining(String category, String title);

    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.status = :condition")
    List<Item> findByCategoryAndCondition(@Param("category") String category, @Param("condition") String condition);

    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.status = :condition AND i.title LIKE %:title%")
    List<Item> findByCategoryAndConditionAndTitleContaining(
            @Param("category") String category,
            @Param("condition") String condition,
            @Param("title") String title
    );
}
