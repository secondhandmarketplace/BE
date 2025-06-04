package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.product.ProductCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductImageRequest;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductResponse;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductUpdateRequest;
import ac.su.kdt.secondhandmarketplace.entity.Category;
import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.entity.ProductImage;
import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import ac.su.kdt.secondhandmarketplace.entity.User;
import ac.su.kdt.secondhandmarketplace.repository.CategoryRepository;
import ac.su.kdt.secondhandmarketplace.repository.ProductImageRepository;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
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
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    // 새로운 상품을 등록
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // 카테고리 ID로 Category 엔티티를 찾습니다. 없으면 예외를 발생
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));

        // AI 예측 카테고리 ID가 있다면 Category 엔티티를 찾습니다.
        Category aiPredictedCategory = null;
        if (request.getAiPredictedCategoryId() != null) {
            aiPredictedCategory = categoryRepository.findById(request.getAiPredictedCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("AI 예측 카테고리를 찾을 수 없습니다: " + request.getAiPredictedCategoryId()));
        }

        // 판매자 ID로 User 엔티티를 찾습니다. 없으면 예외를 발생
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // Product 엔티티를 생성하고 요청 DTO의 값으로 초기화
        Product product = new Product();
        product.setCategory(category); // 카테고리 설정
        product.setAiPredictedCategory(aiPredictedCategory); // AI 예측 카테고리 설정
        product.setUser(user); // 판매자 설정
        product.setTitle(request.getTitle()); // 상품명 설정
        product.setDescription(request.getDescription()); // 상세 설명 설정
        product.setPrice(request.getPrice()); // 가격 설정
        product.setStatus(request.getStatus()); // 상태 설정
        product.setAiPriceMin(request.getAiPriceMin()); // AI 예측 최소 가격 설정
        product.setAiPriceMax(request.getAiPriceMax()); // AI 예측 최대 가격 설정
        product.setLocationInfo(request.getLocationInfo()); // 위치 정보 설정
        product.setCreateAt(LocalDateTime.now()); // 생성 시간 설정 (PrePersist에서 자동 설정되지만 명시적으로도 가능)
        product.setUpdateAt(LocalDateTime.now()); // 업데이트 시간 설정 (PreUpdate에서 자동 설정되지만 명시적으로도 가능)
        product.setViewCount(0); // 조회수 초기화
        product.setChatCount(0); // 채팅 수 초기화

        // 상품 이미지가 있다면 처리
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (ProductImageRequest imageRequest : request.getImageUrls()) {
                ProductImage productImage = new ProductImage(); // 새 ProductImage 엔티티 생성
                productImage.setImageUrl(imageRequest.getImageUrl()); // 이미지 URL 설정
                productImage.setSequence(imageRequest.getSequence()); // 이미지 순서 설정
                productImage.setProduct(product); // Product 엔티티와 연관관계 설정
                product.addImage(productImage); // Product 엔티티의 이미지 리스트에 추가 (양방향 연관관계)
            }
        }

        // 상품을 데이터베이스에 저장
        Product savedProduct = productRepository.save(product);

        // 저장된 상품 엔티티를 응답 DTO로 변환하여 반환
        return ProductResponse.fromEntity(savedProduct);
    }


    //특정 상품의 정보를 조회. 조회수도 1 증가시킵니다.
    @Transactional
    public ProductResponse getProductById(Long productId) {
        // 상품 ID로 Product 엔티티를 찾습니다. 없으면 예외를 발생
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + productId));

        // 조회수 증가
        product.setViewCount(product.getViewCount() + 1);
        // updateAt 자동 갱신 (PreUpdate 어노테이션에 의해 처리)
        productRepository.save(product); // 변경된 조회수를 데이터베이스에 반영

        // 조회된 상품 엔티티를 응답 DTO로 변환하여 반환
        return ProductResponse.fromEntity(product);
    }

    //모든 상품을 조회 (페이징 및 정렬 가능)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        // 모든 상품을 페이징하여 조회하고, 각 Product 엔티티를 ProductResponse DTO로 변환하여 반환합니다.
        return productRepository.findAll(pageable)
                .map(ProductResponse::fromEntity); // Stream API의 map을 사용하여 각 엔티티를 DTO로 변환합니다.
    }

    // 특정 키워드를 포함하는 상품을 검색 (제목 또는 설명)
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        // 제목 또는 설명에 키워드가 포함되고, 상태가 FOR_SALE인 상품을 검색
        return productRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
                        keyword, keyword, ProductStatus.FOR_SALE, pageable)
                .map(ProductResponse::fromEntity); // 검색된 Product 엔티티를 ProductResponse DTO로 변환
    }


    //특정 카테고리의 상품을 조회
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long Id, Pageable pageable) {
        // 카테고리 ID와 상태가 FOR_SALE인 상품을 조회
        return productRepository.findByCategory_IdAndStatus(Id, ProductStatus.FOR_SALE, pageable)
                .map(ProductResponse::fromEntity); // 조회된 Product 엔티티를 ProductResponse DTO로 변환
    }

    //특정 판매자의 상품을 조회
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByUser(Long Id, Pageable pageable) {
        // 사용자 ID와 상태가 FOR_SALE인 상품을 조회
        return productRepository.findByUser_IdAndStatus(Id, ProductStatus.FOR_SALE, pageable)
                .map(ProductResponse::fromEntity); // 조회된 Product 엔티티를 ProductResponse DTO로 변환
    }

    //특정 상품의 정보를 수정
    @Transactional
    public ProductResponse updateProduct(Long Id, ProductUpdateRequest request) {
        // 상품 ID로 Product 엔티티를 찾습니다. 없으면 예외를 발생
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + Id));

        // 요청 DTO의 값이 null이 아니면 해당 필드를 업데이트
        Optional.ofNullable(request.getTitle()).ifPresent(product::setTitle); // 상품명 업데이트
        Optional.ofNullable(request.getDescription()).ifPresent(product::setDescription); // 상세 설명 업데이트
        Optional.ofNullable(request.getPrice()).ifPresent(product::setPrice); // 가격 업데이트
        Optional.ofNullable(request.getStatus()).ifPresent(product::setStatus); // 상태 업데이트
        Optional.ofNullable(request.getAiPriceMin()).ifPresent(product::setAiPriceMin); // AI 예측 최소 가격 업데이트
        Optional.ofNullable(request.getAiPriceMax()).ifPresent(product::setAiPriceMax); // AI 예측 최대 가격 업데이트
        Optional.ofNullable(request.getLocationInfo()).ifPresent(product::setLocationInfo); // 위치 정보 업데이트

        // 상품 이미지가 있다면 기존 이미지를 삭제하고 새로운 이미지로 교체
        if (request.getImageUrls() != null) {
            // 기존 이미지 삭제
            productImageRepository.deleteByProduct_Id(Id);
            product.getImages().clear(); // Product 엔티티의 이미지 리스트도 비웁니다.

            // 새로운 이미지 추가
            for (ProductImageRequest imageRequest : request.getImageUrls()) {
                ProductImage productImage = new ProductImage(); // 새 ProductImage 엔티티 생성
                productImage.setImageUrl(imageRequest.getImageUrl()); // 이미지 URL 설정
                productImage.setSequence(imageRequest.getSequence()); // 이미지 순서 설정
                productImage.setProduct(product); // Product 엔티티와 연관관계 설정
                product.addImage(productImage); // Product 엔티티의 이미지 리스트에 추가 (양방향 연관관계)
            }
        }

        // 업데이트된 상품을 데이터베이스에 저장 (PreUpdate 어노테이션에 의해 updateAt 자동 갱신)
        Product updatedProduct = productRepository.save(product);

        // 업데이트된 상품 엔티티를 응답 DTO로 변환하여 반환
        return ProductResponse.fromEntity(updatedProduct);
    }

    //특정 상품을 삭제 (상태를 'DELETED'로 변경)
    @Transactional
    public void deleteProduct(Long Id) {
        // 상품 ID로 Product 엔티티를 찾습니다. 없으면 예외를 발생
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + Id));

        // 상품 상태를 DELETED로 변경 (실제 삭제가 아닌 논리적 삭제)
        product.setStatus(ProductStatus.DELETED);
        // 변경된 상태를 데이터베이스에 저장
        productRepository.save(product);
    }

    //특정 상품의 'refreshed_at' 필드를 현재 시간으로 업데이트
    @Transactional
    public ProductResponse refreshProduct(Long Id) {
        // 상품 ID로 Product 엔티티를 찾습니다. 없으면 예외를 발생
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + Id));

        // refreshedAt 필드를 현재 시간으로 업데이트
        product.setRefreshedAt(LocalDateTime.now());
        // 업데이트된 상품을 데이터베이스에 저장
        Product refreshedProduct = productRepository.save(product);

        // 새로고침된 상품 엔티티를 응답 DTO로 변환하여 반환
        return ProductResponse.fromEntity(refreshedProduct);
    }

    //특정 상품의 상태를 'SOLD_OUT'으로 변경하고, 'sold_at' 필드를 현재 시간으로 업데이트
    @Transactional
    public ProductResponse markProductAsSold(Long Id) {
        // 상품 ID로 Product 엔티티를 찾습니다. 없으면 예외를 발생
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + Id));

        // 상품 상태를 SOLD_OUT으로 변경하고, soldAt을 현재 시간으로 설정
        product.setStatus(ProductStatus.SOLD_OUT);
        product.setSoldAt(LocalDateTime.now());
        // 변경된 상품을 데이터베이스에 저장
        Product soldProduct = productRepository.save(product);

        // 판매 완료 처리된 상품 엔티티를 응답 DTO로 변환하여 반환
        return ProductResponse.fromEntity(soldProduct);
    }
}