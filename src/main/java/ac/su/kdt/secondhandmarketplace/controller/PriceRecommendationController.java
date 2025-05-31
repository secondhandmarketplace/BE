package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.dto.PriceRecommendationRequest;
import ac.su.kdt.secondhandmarketplace.dto.PriceRecommendationResponse;
import ac.su.kdt.secondhandmarketplace.service.PriceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
  가격 추천 API 사용 예시:
 
  1. 새로운 상품 가격 추천 요청:
  curl -X POST http://localhost:8080/api/v1/price-recommendations \
    -H "Content-Type: application/json" \
    -d '{
      "userRequest": "아이폰 13 프로 맥스 256기가 중고 가격 추천해줘",
      "category": "전자기기",
      "condition": "중고"
    }'
  
 * 2. 기존 상품 가격 조정 추천 요청:
 * curl -X POST http://localhost:8080/api/v1/price-recommendations/123/adjust \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "userRequest": "현재 가격이 너무 비싼 것 같아 조정이 필요해",
 *     "currentPrice": 1200000,
 *     "condition": "중고"
 *   }'
 */

/**
 * 가격 추천 API 컨트롤러
 * 상품의 적정 판매가를 추천받기 위한 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/price-recommendations")
@RequiredArgsConstructor
@Slf4j
public class PriceRecommendationController {

    private final PriceRecommendationService priceRecommendationService;

    /**
     * 새로운 상품의 적정가 추천을 요청합니다.
     * 
     * @param request 가격 추천 요청 정보 (상품 카테고리, 상태, 사용자 요청 등)
     * @return 가격 추천 결과 (추천 최소/최대/평균 가격, 가격 결정 요소, 시장 전략 등)
     */
    @PostMapping
    public Mono<ResponseEntity<PriceRecommendationResponse>> getPriceRecommendation(
            @RequestBody PriceRecommendationRequest request) {
        return priceRecommendationService.getPriceRecommendation(request)
                .flatMap(response ->
                        Mono.just(ResponseEntity.ok(response))
                )
                .switchIfEmpty(
                        Mono.just(
                                ResponseEntity
                                        .<PriceRecommendationResponse>noContent()
                                        .build()
                        )
                )
                .onErrorResume(e -> {
                    log.error("가격 추천 처리 중 오류 발생: {}", e.getMessage(), e);
                    return Mono.just(
                            ResponseEntity
                                    .<PriceRecommendationResponse>internalServerError()
                                    .build()
                    );
                });
    }



    /**
     * 기존 상품의 가격 조정 추천을 요청합니다.
     * 
     * @param productId 상품 ID (가격 조정이 필요한 기존 상품의 고유 식별자)
     * @param request 가격 추천 요청 정보 (현재 가격, 상품 상태, 사용자 요청 등)
     * @return 가격 추천 결과 (가격 조정 제안, 시장 상황 분석, 가격 결정 요소의 영향도 등)
     */

    @PostMapping("/{productId}/adjust")
    public Mono<ResponseEntity<PriceRecommendationResponse>> getPriceAdjustmentRecommendation(
            @PathVariable Long productId,
            @RequestBody PriceRecommendationRequest request) {
        request.setProductId(productId);
        return priceRecommendationService.getPriceRecommendation(request)
                .flatMap(response ->
                        Mono.just(ResponseEntity.<PriceRecommendationResponse>ok(response))
                )
                .switchIfEmpty(
                        Mono.just(
                                ResponseEntity.<PriceRecommendationResponse>noContent().build()
                        )
                )
                .onErrorResume(e -> {
                    log.error("가격 조정 추천 오류: {}", e.getMessage(), e);
                    return Mono.just(
                            ResponseEntity.<PriceRecommendationResponse>internalServerError().build()
                    );
                });
    }


}