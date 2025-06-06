package com.kdt.backend.controller;

import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceRecommendationResponse;
import com.kdt.backend.service.PriceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/price-recommendations")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class PriceRecommendationController {

    private final PriceRecommendationService priceRecommendationService;

    /**
     * 직접 가격 추천 (AI 없이, 빠른 응답)
     * 사용 예시: "에어팟2세대 S급 2년정도 사용했어 얼마에 파는게 좋아?"
     */
    @PostMapping("/direct")
    public ResponseEntity<Map<String, Object>> getDirectPriceRecommendation(
            @RequestBody Map<String, String> request) {

        String userRequest = request.get("message");

        try {
            log.info("직접 가격 추천 요청: {}", userRequest);

            String response = priceRecommendationService.getDirectPriceRecommendation(userRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("type", "DIRECT_PRICE_RECOMMENDATION");
            result.put("dataSource", "INTERNAL_DB_ONLY");
            result.put("sortedBy", "latest");
            result.put("timestamp", LocalDateTime.now());
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("직접 가격 추천 실패: {}", e.getMessage());

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("response", "가격 추천 중 오류가 발생했습니다.");
            errorResult.put("error", e.getMessage());
            errorResult.put("success", false);
            errorResult.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 새로운 상품의 적정가 추천을 요청합니다 (AI 기반)
     */
    @PostMapping
    public Mono<ResponseEntity<PriceRecommendationResponse>> getPriceRecommendation(
            @RequestBody PriceRecommendationRequest request) {

        log.info("AI 기반 가격 추천 요청: {}", request.getUserRequest());

        return priceRecommendationService.getPriceRecommendation(request)
                .map(response -> {
                    log.info("AI 가격 추천 완료: {}원 ~ {}원",
                            response.getRecommendedMinPrice(), response.getRecommendedMaxPrice());
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(
                        Mono.fromCallable(() -> {
                            log.warn("AI 가격 추천 결과 없음");
                            return ResponseEntity.<PriceRecommendationResponse>noContent().build();
                        })
                )
                .onErrorResume(e -> {
                    log.error("AI 가격 추천 처리 중 오류 발생: {}", e.getMessage(), e);
                    return Mono.just(
                            ResponseEntity.<PriceRecommendationResponse>internalServerError().build()
                    );
                });
    }

    /**
     * 고급 가격 추천 (내부/외부 데이터 분석 포함)
     */
    @PostMapping("/advanced")
    public Mono<ResponseEntity<PriceRecommendationResponse>> getAdvancedPriceRecommendation(
            @RequestBody PriceRecommendationRequest request) {

        log.info("고급 가격 추천 요청: {}", request.getUserRequest());

        return priceRecommendationService.getAdvancedPriceRecommendation(request)
                .map(response -> {
                    log.info("고급 가격 추천 완료");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("고급 가격 추천 실패: {}", e.getMessage());
                    return Mono.just(ResponseEntity.<PriceRecommendationResponse>internalServerError().build());
                });
    }

    /**
     * 기존 상품의 가격 조정 추천을 요청합니다
     */
    @PostMapping("/{productId}/adjust")
    public Mono<ResponseEntity<PriceRecommendationResponse>> getPriceAdjustmentRecommendation(
            @PathVariable Long productId,
            @RequestBody PriceRecommendationRequest request) {

        log.info("가격 조정 추천 요청: productId={}, request={}", productId, request.getUserRequest());

        request.setProductId(productId);

        return priceRecommendationService.getPriceRecommendation(request)
                .map(response -> {
                    log.info("가격 조정 추천 완료: productId={}", productId);
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(
                        Mono.fromCallable(() -> {
                            log.warn("가격 조정 추천 결과 없음: productId={}", productId);
                            return ResponseEntity.<PriceRecommendationResponse>noContent().build();
                        })
                )
                .onErrorResume(e -> {
                    log.error("가격 조정 추천 오류: productId={}, error={}", productId, e.getMessage(), e);
                    return Mono.just(
                            ResponseEntity.<PriceRecommendationResponse>internalServerError().build()
                    );
                });
    }

    /**
     * 가격 추천 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPriceRecommendationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "price-recommendation");
        status.put("status", "active");
        status.put("dataSource", "INTERNAL_DB_ONLY");
        status.put("sortedBy", "latest");
        status.put("timestamp", LocalDateTime.now());
        status.put("supportedProducts", new String[]{
                "에어팟", "아이폰", "갤럭시", "바나나", "일반상품"
        });
        status.put("supportedConditions", new String[]{
                "S급", "A급", "B급", "C급", "새상품", "중고"
        });
        status.put("availableEndpoints", new String[]{
                "/direct", "/", "/advanced", "/{productId}/adjust", "/status"
        });

        return ResponseEntity.ok(status);
    }

    /**
     * 가격 추천 테스트 (개발용)
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testPriceRecommendation(
            @RequestBody Map<String, String> request) {

        String userRequest = request.get("message");

        Map<String, Object> result = new HashMap<>();
        result.put("originalRequest", userRequest);
        result.put("extractedProduct", extractProductInfo(userRequest));
        result.put("extractedCondition", extractConditionInfo(userRequest));
        result.put("extractedCategory", extractCategoryInfo(userRequest));
        result.put("isPriceRequest", isPriceRecommendationRequest(userRequest));
        result.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(result);
    }

    private String extractProductInfo(String userRequest) {
        String lower = userRequest.toLowerCase();
        if (lower.contains("에어팟")) return "에어팟";
        if (lower.contains("아이폰")) return "아이폰";
        if (lower.contains("갤럭시")) return "갤럭시";
        if (lower.contains("바나나")) return "바나나";
        return "일반 상품";
    }

    private String extractConditionInfo(String userRequest) {
        String lower = userRequest.toLowerCase();
        if (lower.contains("s급")) return "S급";
        if (lower.contains("a급")) return "A급";
        if (lower.contains("b급")) return "B급";
        if (lower.contains("c급")) return "C급";
        return "중고";
    }

    private String extractCategoryInfo(String userRequest) {
        String lower = userRequest.toLowerCase();
        if (lower.contains("아이폰") || lower.contains("에어팟") || lower.contains("갤럭시")) {
            return "전자기기";
        }
        if (lower.contains("바나나") || lower.contains("과일")) {
            return "식품";
        }
        return "기타";
    }

    private boolean isPriceRecommendationRequest(String userRequest) {
        String[] priceKeywords = {"얼마에 팔", "가격", "얼마", "시세", "추천", "적정가", "팔까"};
        String lower = userRequest.toLowerCase();

        for (String keyword : priceKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
