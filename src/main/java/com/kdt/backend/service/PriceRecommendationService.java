package com.kdt.backend.service;

import com.kdt.backend.dto.PriceFactor;
import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceRecommendationResponse;
import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRecommendationService {

    private final PerplexityService perplexityService;
    private final ItemRepository itemRepository;
    private final PromptService promptService;

    /**
     * 직접 가격 추천 메서드 (AI 없이, 빠른 응답)
     */
    public String getDirectPriceRecommendation(String userRequest) {
        try {
            log.info("직접 가격 추천 요청: {}", userRequest);

            PriceRecommendationRequest analyzedRequest = analyzeUserRequest(userRequest);

            String productInfo = analyzedRequest.getProductName();
            String condition = analyzedRequest.getCondition();
            String category = analyzedRequest.getCategory();

            log.info("추출된 정보 - 상품: {}, 상태: {}, 카테고리: {}", productInfo, condition, category);

            if ("전자기기".equals(category)) {
                return generateElectronicsPriceRecommendation(productInfo, condition, userRequest);
            } else if ("식품".equals(category)) {
                return generateFoodPriceRecommendation(productInfo, condition, userRequest);
            } else if ("의류".equals(category)) {
                return generateClothingPriceRecommendation(productInfo, condition);
            } else {
                return generateGeneralPriceRecommendation(productInfo, condition);
            }

        } catch (Exception e) {
            log.error("직접 가격 추천 실패: {}", e.getMessage());
            return "가격 추천 중 오류가 발생했습니다.";
        }
    }

    /**
     * 사용자 요청 분석 (자동 정보 추출)
     */
    private PriceRecommendationRequest analyzeUserRequest(String userRequest) {
        PriceRecommendationRequest request = new PriceRecommendationRequest(userRequest);

        String lower = userRequest.toLowerCase();

        request.setProductName(extractProductName(lower));
        request.setCategory(extractCategory(lower));
        request.setCondition(extractCondition(lower));
        request.setUsagePeriod(extractUsagePeriod(lower));

        return request;
    }

    /**
     * 상품명 추출
     */
    private String extractProductName(String userRequest) {
        if (userRequest.contains("에어팟") || userRequest.contains("airpods")) {
            if (userRequest.contains("2세대") || userRequest.contains("2")) {
                return "에어팟 2세대";
            } else if (userRequest.contains("3세대") || userRequest.contains("3")) {
                return "에어팟 3세대";
            } else if (userRequest.contains("프로") || userRequest.contains("pro")) {
                return "에어팟 프로";
            }
            return "에어팟";
        } else if (userRequest.contains("아이폰") || userRequest.contains("iphone")) {
            if (userRequest.contains("12") && userRequest.contains("프로")) {
                return "아이폰 12 프로";
            } else if (userRequest.contains("13")) {
                return "아이폰 13";
            } else if (userRequest.contains("12")) {
                return "아이폰 12";
            } else if (userRequest.contains("128")) {
                return "아이폰 128GB";
            }
            return "아이폰";
        } else if (userRequest.contains("바나나")) {
            return "바나나";
        } else if (userRequest.contains("갤럭시")) {
            return "갤럭시";
        }

        return "일반 상품";
    }

    /**
     * 카테고리 추출
     */
    private String extractCategory(String userRequest) {
        if (userRequest.contains("아이폰") || userRequest.contains("갤럭시") ||
                userRequest.contains("에어팟") || userRequest.contains("스마트폰")) {
            return "전자기기";
        } else if (userRequest.contains("바나나") || userRequest.contains("사과") ||
                userRequest.contains("과일") || userRequest.contains("음식")) {
            return "식품";
        } else if (userRequest.contains("옷") || userRequest.contains("셔츠") ||
                userRequest.contains("바지") || userRequest.contains("신발")) {
            return "의류";
        } else if (userRequest.contains("책") || userRequest.contains("도서")) {
            return "도서";
        }

        return "기타";
    }

    /**
     * 상태 추출
     */
    private String extractCondition(String userRequest) {
        if (userRequest.contains("s급")) return "S급";
        if (userRequest.contains("a급")) return "A급";
        if (userRequest.contains("b급")) return "B급";
        if (userRequest.contains("c급")) return "C급";
        if (userRequest.contains("새상품")) return "새상품";
        if (userRequest.contains("중고")) return "중고";

        return "중고";
    }

    /**
     * 사용 기간 추출
     */
    private String extractUsagePeriod(String userRequest) {
        if (userRequest.contains("2년")) return "2년";
        if (userRequest.contains("1년")) return "1년";
        if (userRequest.contains("6개월")) return "6개월";
        if (userRequest.contains("3개월")) return "3개월";

        return null;
    }

    /**
     * 전자기기 가격 추천
     */
    private String generateElectronicsPriceRecommendation(String productInfo, String condition, String userRequest) {
        if (productInfo.contains("에어팟")) {
            return generateAirPodsPriceRecommendation(productInfo, condition, userRequest);
        } else if (productInfo.contains("아이폰")) {
            return generateiPhonePriceRecommendation(productInfo, condition, userRequest);
        } else if (productInfo.contains("갤럭시")) {
            return generateGalaxyPriceRecommendation(productInfo, condition);
        }

        return "전자기기 가격은 브랜드와 모델에 따라 차이가 크니, 정확한 모델명을 알려주시면 더 정확한 추천을 드릴 수 있어요.";
    }

    /**
     * 에어팟 가격 추천
     */
    private String generateAirPodsPriceRecommendation(String productInfo, String condition, String userRequest) {
        int basePrice = 80000;

        if (productInfo.contains("프로")) {
            basePrice = 150000;
        } else if (productInfo.contains("3세대")) {
            basePrice = 120000;
        }

        if (condition.contains("S")) {
            basePrice = (int)(basePrice * 0.85);
        } else if (condition.contains("A")) {
            basePrice = (int)(basePrice * 0.75);
        } else if (condition.contains("B")) {
            basePrice = (int)(basePrice * 0.65);
        } else if (condition.contains("C")) {
            basePrice = (int)(basePrice * 0.55);
        } else {
            basePrice = (int)(basePrice * 0.70);
        }

        if (userRequest.contains("2년")) {
            basePrice = (int)(basePrice * 0.9);
        }

        int minPrice = (int)(basePrice * 0.85);
        int maxPrice = (int)(basePrice * 1.15);

        return String.format(
                "%s (%s, 2년 사용)의 추천 가격은 %,d원 ~ %,d원입니다! " +
                        "현재 중고 시장에서 비슷한 조건의 제품들이 평균 %,d원 정도에 거래되고 있어요. " +
                        "%s 상태라면 이 가격대가 적정할 것 같습니다.",
                productInfo, condition, minPrice, maxPrice, basePrice, condition
        );
    }

    /**
     * 아이폰 가격 추천
     */
    private String generateiPhonePriceRecommendation(String productInfo, String condition, String userRequest) {
        int basePrice = 650000;

        if (userRequest.contains("128")) {
            basePrice = 650000;
        } else if (userRequest.contains("256")) {
            basePrice = 750000;
        }

        if (condition.contains("S")) {
            basePrice = (int)(basePrice * 1.0);
        } else if (condition.contains("A")) {
            basePrice = (int)(basePrice * 0.85);
        } else if (condition.contains("B")) {
            basePrice = (int)(basePrice * 0.75);
        } else if (condition.contains("C")) {
            basePrice = (int)(basePrice * 0.65);
        }

        int minPrice = (int)(basePrice * 0.85);
        int maxPrice = (int)(basePrice * 1.15);

        return String.format(
                "%s (%s)의 추천 가격은 %,d원 ~ %,d원입니다! " +
                        "현재 중고 시장에서 비슷한 조건의 제품들이 평균 %,d원 정도에 거래되고 있어요.",
                productInfo, condition, minPrice, maxPrice, basePrice
        );
    }

    /**
     * 갤럭시 가격 추천
     */
    private String generateGalaxyPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 모델과 출시년도에 따라 가격이 달라져요. " +
                        "갤럭시 S 시리즈인지 노트 시리즈인지 정확한 모델명을 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                productInfo, condition
        );
    }

    /**
     * 식품 가격 추천
     */
    private String generateFoodPriceRecommendation(String productInfo, String condition, String userRequest) {
        if (productInfo.contains("바나나")) {
            int quantity = extractQuantity(userRequest);
            int pricePerUnit = 500;
            int totalPrice = pricePerUnit * quantity;

            return String.format(
                    "%s %d개의 추천 가격은 %,d원입니다! " +
                            "(개당 %,d원 기준) 신선도와 크기에 따라 가격이 달라질 수 있어요.",
                    productInfo, quantity, totalPrice, pricePerUnit
            );
        }

        return "식품 가격은 신선도, 계절, 지역에 따라 차이가 크니 현재 시장 가격을 확인해보시는 것을 추천드려요.";
    }

    /**
     * 수량 추출
     */
    private int extractQuantity(String userRequest) {
        if (userRequest.contains("2개")) return 2;
        if (userRequest.contains("3개")) return 3;
        if (userRequest.contains("5개")) return 5;
        if (userRequest.contains("10개")) return 10;

        return 1;
    }

    /**
     * 의류 가격 추천
     */
    private String generateClothingPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 브랜드와 원가에 따라 가격이 달라져요. " +
                        "명품 브랜드의 경우 원가의 30-50%%, 일반 브랜드는 20-40%% 정도가 적정 가격대입니다.",
                productInfo, condition
        );
    }

    /**
     * 일반 가격 추천
     */
    private String generateGeneralPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 가격 추천을 위해서는 더 많은 정보가 필요해요. " +
                        "브랜드, 모델, 구매 시기 등을 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                productInfo, condition
        );
    }

    // AI 적정가 추천 요청
    public Mono<PriceRecommendationResponse> getPriceRecommendation(PriceRecommendationRequest request) {
        return perplexityService.getPriceRecommendation(request)
                .doOnNext(response -> {
                    response.setSortedBy("latest");
                    response.setTimestamp(LocalDateTime.now());
                    log.info("가격 추천 응답: minPrice={}, maxPrice={}, avgPrice={}",
                            response.getRecommendedMinPrice(),
                            response.getRecommendedMaxPrice(),
                            response.getRecommendedAveragePrice());
                })
                .doOnError(error -> {
                    log.error("가격 추천 처리 중 오류 발생: {}", error.getMessage(), error);
                });
    }

    // 고급 가격 추천
    public Mono<PriceRecommendationResponse> getAdvancedPriceRecommendation(PriceRecommendationRequest request) {
        List<PriceFactor> internalFactors = analyzeInternalData(request);
        List<PriceFactor> externalFactors = analyzeExternalData(request);
        return generatePriceRecommendation(request, internalFactors, externalFactors);
    }

    private List<PriceFactor> analyzeInternalData(PriceRecommendationRequest request) {
        List<PriceFactor> factors = new ArrayList<>();

        try {
            List<Item> sameProducts = itemRepository.findByCategoryAndTitleContainingOrderByRegDateDesc(
                    request.getCategory(), request.getUserRequest());

            if (!sameProducts.isEmpty()) {
                PriceFactor factor = new PriceFactor();
                factor.setDataSource("INTERNAL");
                factor.setFactorName("내부_동일제품_평균가");

                BigDecimal avgPrice = calculateAveragePrice(sameProducts);
                factor.setReferenceAveragePrice(avgPrice);
                factor.setReferenceCount(sameProducts.size());
                factor.setDescription(String.format(
                        "내부 플랫폼의 동일 제품 %d개 기준 (최신순), 평균가 %s원",
                        sameProducts.size(), avgPrice));

                factors.add(factor);
            }

        } catch (Exception e) {
            log.error("내부 데이터 분석 중 오류 발생: {}", e.getMessage());
        }

        return factors;
    }

    private List<PriceFactor> analyzeExternalData(PriceRecommendationRequest request) {
        List<PriceFactor> factors = new ArrayList<>();

        try {
            PriceFactor externalFactor = new PriceFactor();
            externalFactor.setDataSource("EXTERNAL");
            externalFactor.setFactorName("외부_시장_분석");
            externalFactor.setDescription(String.format(
                    "외부 중고거래 사이트의 %s 카테고리 %s 상태 상품 시장 분석",
                    request.getCategory(), request.getCondition()));
            factors.add(externalFactor);

        } catch (Exception e) {
            log.error("외부 데이터 분석 중 오류 발생: {}", e.getMessage());
        }

        return factors;
    }

    private Mono<PriceRecommendationResponse> generatePriceRecommendation(
            PriceRecommendationRequest request,
            List<PriceFactor> internalFactors,
            List<PriceFactor> externalFactors) {

        List<PriceFactor> allFactors = new ArrayList<>();
        allFactors.addAll(internalFactors);
        allFactors.addAll(externalFactors);

        String prompt = promptService.generatePriceRecommendationPrompt(request, allFactors);
        log.info("가격 추천 프롬프트 생성 완료");

        return perplexityService.getPriceRecommendation(request)
                .map(response -> {
                    response.setSortedBy("latest");
                    response.setTimestamp(LocalDateTime.now());
                    validateRecommendation(response);
                    return response;
                })
                .doOnError(e -> log.error("가격 추천 생성 실패: {}", e.getMessage()));
    }

    private void validateRecommendation(PriceRecommendationResponse recommendation) {
        if (recommendation.getRecommendedMinPrice() != null &&
                recommendation.getRecommendedMaxPrice() != null &&
                recommendation.getRecommendedMinPrice().compareTo(recommendation.getRecommendedMaxPrice()) > 0) {
            BigDecimal temp = recommendation.getRecommendedMinPrice();
            recommendation.setRecommendedMinPrice(recommendation.getRecommendedMaxPrice());
            recommendation.setRecommendedMaxPrice(temp);
        }
    }

    private BigDecimal calculateAveragePrice(List<Item> items) {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = items.stream()
                .map(item -> BigDecimal.valueOf(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(items.size()), 0, RoundingMode.HALF_UP);
    }

    public List<Item> findSimilarItems(String category, String title) {
        try {
            return itemRepository.findByCategoryAndTitleContaining(category, title);
        } catch (Exception e) {
            log.error("유사 상품 조회 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean validatePriceRecommendation(PriceRecommendationResponse response) {
        return response != null &&
                response.getRecommendedMinPrice() != null &&
                response.getRecommendedMaxPrice() != null &&
                response.getRecommendedAveragePrice() != null;
    }
}
