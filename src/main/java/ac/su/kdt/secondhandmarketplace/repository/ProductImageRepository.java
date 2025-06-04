package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // 특정 상품 ID에 해당하는 모든 이미지를 순서(sequence)에 따라 오름차순으로 조회
    List<ProductImage> findByProduct_IdOrderBySequence(Long product_id);

    // 특정 상품 ID와 이미지 URL에 해당하는 이미지가 존재하는지 확인.
    boolean existsByProduct_IdAndImageUrl(Long productId, String imageUrl);

    // 특정 상품 ID에 해당하는 모든 이미지들을 삭제
    void deleteByProduct_Id(Long product_id);
}