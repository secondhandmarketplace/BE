package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.dto.product.ProductCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductResponse;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductUpdateRequest;
import ac.su.kdt.secondhandmarketplace.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //새 상품을 등록
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        ProductResponse product = productService.createProduct(request); // Service 계층에 상품 생성을 요청
        return new ResponseEntity<>(product, HttpStatus.CREATED); // 생성된 상품 정보와 201 Created 상태 코드를 반환
    }

    //특정 ID의 상품을 조회. 조회수를 1 증가
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        ProductResponse product = productService.getProductById(productId); // Service 계층에 상품 조회를 요청
        return ResponseEntity.ok(product); // 조회된 상품 정보와 200 OK 상태 코드를 반환
    }

    //모든 상품을 페이징하여 조회
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable); // Service 계층에 모든 상품 조회를 요청
        return ResponseEntity.ok(products); // 페이지네이션된 상품 목록과 200 OK 상태 코드를 반환
    }

    //키워드로 상품을 검색 (제목 또는 설명)
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = productService.searchProducts(keyword, pageable); // Service 계층에 상품 검색을 요청
        return ResponseEntity.ok(products); // 검색된 상품 목록과 200 OK 상태 코드를 반환
    }

    //특정 카테고리의 상품을 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable); // Service 계층에 카테고리별 상품 조회를 요청
        return ResponseEntity.ok(products); // 조회된 상품 목록과 200 OK 상태 코드를 반환
    }

    //특정 판매자(사용자)의 상품을 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByUser(
            @PathVariable Long userId,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsByUser(userId, pageable); // Service 계층에 판매자별 상품 조회를 요청
        return ResponseEntity.ok(products); // 조회된 상품 목록과 200 OK 상태 코드를 반환
    }

    //특정 ID의 상품 정보를 업데이트
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(productId, request); // Service 계층에 상품 업데이트를 요청
        return ResponseEntity.ok(updatedProduct); // 업데이트된 상품 정보와 200 OK 상태 코드를 반환
    }

    //특정 ID의 상품을 삭제 (상태를 'DELETED'로 변경)
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId); // Service 계층에 상품 삭제를 요청
        return ResponseEntity.noContent().build(); // 204 No Content 상태 코드를 반환. (응답 본문 없음)
    }

    //특정 ID의 상품을 새로고침 (refreshed_at 필드를 현재 시간으로 업데이트)
    @PatchMapping("/{productId}/refresh") // HTTP PATCH 요청을 처리하며, 특정 리소스의 일부만 수정할 때 사용
    public ResponseEntity<ProductResponse> refreshProduct(@PathVariable Long productId) {
        ProductResponse refreshedProduct = productService.refreshProduct(productId); // Service 계층에 상품 새로고침을 요청
        return ResponseEntity.ok(refreshedProduct); // 새로고침된 상품 정보와 200 OK 상태 코드를 반환
    }

    //특정 ID의 상품 상태를 'SOLD_OUT'으로 변경합니다.
    @PatchMapping("/{productId}/sold-out")
    public ResponseEntity<ProductResponse> markProductAsSold(@PathVariable Long productId) {
        ProductResponse soldProduct = productService.markProductAsSold(productId); // Service 계층에 상품 판매 완료를 요청
        return ResponseEntity.ok(soldProduct); // 판매 완료 처리된 상품 정보와 200 OK 상태 코드를 반환
    }
}