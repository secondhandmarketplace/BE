package com.kdt.backend.service;

import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceRecommendationResponse;
import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRecommendationService {

    private final PerplexityService perplexityService;
    private final ItemRepository itemRepository;

    /**
     * AI 적정가 추천 요청
     */
    public Mono<PriceRecommendationResponse> getPriceRecommendation(PriceRecommendationRequest request) {
        return perplexityService.getPriceRecommendation(request)
                .doOnNext(response -> {
                    // keySet() 대신 각 필드를 안전하게 출력
                    log.info("응답 구조: minPrice={}, maxPrice={}, avgPrice={}, factors={}, impacts={}, suggestion={}, strategy={}, originalResponse={}",
                            response.getRecommendedMinPrice(),
                            response.getRecommendedMaxPrice(),
                            response.getRecommendedAveragePrice(),
                            response.getPriceFactors(),
                            response.getPriceImpacts(),
                            response.getPriceAdjustmentSuggestion(),
                            response.getMarketStrategy(),
                            response.getOriginalResponse()
                    );
                })
                .doOnError(error -> {
                    log.error("가격 추천 처리 중 오류 발생: {}", error.getMessage(), error);
                });
    }

    /**
     * 카테고리와 제목 일부가 일치하는 유사 상품 리스트 조회
     */
    public List<Item> findSimilarItems(String category, String title) {
        try {
            return itemRepository.findByCategoryAndTitleContaining(category, title);
        } catch (Exception e) {
            log.error("유사 상품 조회 중 오류 발생: category={}, title={}, error={}", category, title, e.getMessage());
            return List.of();
        }
    }

    /**
     * 카테고리와 상태가 같은 유사 상품 리스트 조회
     */
    public List<Item> findSimilarConditionItems(String category, String status) {
        try {
            return itemRepository.findByCategoryAndCondition(category, status);
        } catch (Exception e) {
            log.error("유사 상태 상품 조회 중 오류 발생: category={}, status={}, error={}", category, status, e.getMessage());
            return List.of();
        }
    }

    /**
     * 가격 추천을 위한 시장 데이터 분석
     */
    public Mono<PriceRecommendationResponse> analyzePriceWithMarketData(PriceRecommendationRequest request) {
        log.info("시장 데이터 분석 시작: {}", request.getUserRequest());

        return getPriceRecommendation(request)
                .map(response -> {
                    // 추가적인 시장 분석 로직
                    log.info("시장 데이터 분석 완료");
                    return response;
                });
    }

    /**
     * 가격 추천 결과 검증
     */
    public boolean validatePriceRecommendation(PriceRecommendationResponse response) {
        if (response == null) {
            log.warn("가격 추천 응답이 null입니다.");
            return false;
        }

        if (response.getRecommendedMinPrice() == null ||
                response.getRecommendedMaxPrice() == null ||
                response.getRecommendedAveragePrice() == null) {
            log.warn("필수 가격 정보가 누락되었습니다.");
            return false;
        }

        log.info("가격 추천 결과 검증 완료");
        return true;
    }
}
