package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.*;
import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PriceRecommendationService {
    
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final PerplexityService perplexityService;
    private final PromptService promptService;

    /**
     * 상품의 적정가를 추천하는 메인 메소드
     * 1. 내부 플랫폼 데이터 분석
     * 2. 외부 중고거래 사이트 데이터 분석
     * 3. AI를 통한 가격 추천
     */
    public Mono<PriceRecommendationResponse> getPriceRecommendation(PriceRecommendationRequest request) {
        // 1. 내부 플랫폼 데이터 분석
        List<PriceFactor> internalFactors = analyzeInternalData(request);
        
        // 2. 외부 중고거래 사이트 데이터 분석
        List<PriceFactor> externalFactors = analyzeExternalData(request);
        
        // 3. AI를 통한 가격 추천
        return generatePriceRecommendation(request, internalFactors, externalFactors);
    }

    /**
     * 내부 플랫폼 데이터를 분석하여 가격 결정 요소를 추출
     */
    private List<PriceFactor> analyzeInternalData(PriceRecommendationRequest request) {
        List<PriceFactor> factors = new ArrayList<>();
        
        // 1. 동일 제품 분석
        List<Product> sameProducts = productRepository.findByCategoryAndTitleContaining(
            request.getCategory(), 
            request.getUserRequest()
        );
        
        if (!sameProducts.isEmpty()) {
            PriceFactor sameProductFactor = new PriceFactor();
            sameProductFactor.setDataSource("INTERNAL");
            sameProductFactor.setFactorName("내부_동일제품_평균가");
            
            // 가격 통계 계산
            BigDecimal avgPrice = calculateAveragePrice(sameProducts);
            BigDecimal minPrice = findMinPrice(sameProducts);
            BigDecimal maxPrice = findMaxPrice(sameProducts);
            
            sameProductFactor.setReferenceCount(sameProducts.size());
            sameProductFactor.setReferenceAveragePrice(avgPrice);
            sameProductFactor.setReferenceMinPrice(minPrice);
            sameProductFactor.setReferenceMaxPrice(maxPrice);
            
            // 가격 영향도 계산 (현재 가격과의 차이)
            if (request.getCurrentPrice() != null) {
                BigDecimal priceDiff = avgPrice.subtract(request.getCurrentPrice())
                    .divide(request.getCurrentPrice(), 4, RoundingMode.HALF_UP);
                sameProductFactor.setImpact(priceDiff);
            }
            
            sameProductFactor.setDescription(String.format(
                "내부 플랫폼의 동일 제품 %d개 기준, 평균가 %s원 (범위: %s원 ~ %s원)",
                sameProducts.size(), avgPrice, minPrice, maxPrice
            ));
            
            factors.add(sameProductFactor);
        }
        
        // 2. 유사 상태 제품 분석
        List<Product> similarConditionProducts = productRepository.findByCategoryAndCondition(
            request.getCategory(),
            request.getCondition()
        );
        
        if (!similarConditionProducts.isEmpty()) {
            PriceFactor conditionFactor = new PriceFactor();
            conditionFactor.setDataSource("INTERNAL");
            conditionFactor.setFactorName("내부_상품상태_분석");
            
            BigDecimal avgPrice = calculateAveragePrice(similarConditionProducts);
            conditionFactor.setReferenceCount(similarConditionProducts.size());
            conditionFactor.setReferenceAveragePrice(avgPrice);
            
            conditionFactor.setDescription(String.format(
                "내부 플랫폼의 %s 상태 제품 %d개 기준, 평균가 %s원",
                request.getCondition(), similarConditionProducts.size(), avgPrice
            ));
            
            factors.add(conditionFactor);
        }
        
        return factors;
    }

    /**
     * 외부 중고거래 사이트 데이터를 분석하여 가격 결정 요소를 추출
     * Perplexity API가 자체적으로 외부 데이터를 수집하고 분석하므로,
     * 여기서는 기본적인 가격 결정 요소만 제공
     */
    private List<PriceFactor> analyzeExternalData(PriceRecommendationRequest request) {
        List<PriceFactor> factors = new ArrayList<>();
        
        // 기본적인 외부 시장 데이터 요소 추가
        PriceFactor externalFactor = new PriceFactor();
        externalFactor.setDataSource("EXTERNAL");
        externalFactor.setFactorName("외부_시장_분석");
        externalFactor.setDescription(
            String.format("외부 중고거래 사이트의 %s 카테고리 %s 상태 상품 시장 분석",
                request.getCategory(),
                request.getCondition()
            )
        );
        
        factors.add(externalFactor);
        
        return factors;
    }

    /**
     * AI를 통한 최종 가격 추천 생성
     */
    private Mono<PriceRecommendationResponse> generatePriceRecommendation(
        PriceRecommendationRequest request,
        List<PriceFactor> internalFactors,
        List<PriceFactor> externalFactors
    ) {
        // 모든 가격 결정 요소 결합
        List<PriceFactor> allFactors = new ArrayList<>();
        allFactors.addAll(internalFactors);
        allFactors.addAll(externalFactors);
        
        // 프롬프트 생성
        String prompt = promptService.generatePriceRecommendationPrompt(request, allFactors);
        
        // AI API 호출
        return perplexityService.chat(prompt)
            .map(response -> {
                PriceRecommendationResponse recommendation = new PriceRecommendationResponse();
                
                try {
                    // AI 응답에서 가격 정보 추출
                    String responseText = (String) response.get("text");
                    String[] lines = responseText.split("\n");
                    for (String line : lines) {
                        if (line.contains("최소 가격:")) {
                            String price = line.replaceAll("[^0-9]", "");
                            recommendation.setRecommendedMinPrice(new BigDecimal(price));
                        } else if (line.contains("최대 가격:")) {
                            String price = line.replaceAll("[^0-9]", "");
                            recommendation.setRecommendedMaxPrice(new BigDecimal(price));
                        } else if (line.contains("평균 가격:")) {
                            String price = line.replaceAll("[^0-9]", "");
                            recommendation.setRecommendedAveragePrice(new BigDecimal(price));
                        }
                    }
                    
                    // 가격 결정 근거 추출
                    List<String> factors = new ArrayList<>();
                    boolean isFactorSection = false;
                    for (String line : lines) {
                        if (line.contains("2. 가격 결정 근거")) {
                            isFactorSection = true;
                            continue;
                        } else if (line.contains("3. 가격 영향 요소 분석")) {
                            isFactorSection = false;
                            continue;
                        }
                        
                        if (isFactorSection && line.trim().startsWith("-")) {
                            factors.add(line.trim().substring(1).trim());
                        }
                    }
                    recommendation.setPriceFactors(factors);
                    
                    // 가격 영향도 추출
                    List<PriceRecommendationResponse.PriceImpact> impacts = new ArrayList<>();
                    boolean isImpactSection = false;
                    for (String line : lines) {
                        if (line.contains("3. 가격 영향 요소 분석")) {
                            isImpactSection = true;
                            continue;
                        } else if (line.contains("4. 가격 조정 제안") || line.contains("5. 시장 전략 제안")) {
                            isImpactSection = false;
                            continue;
                        }
                        
                        if (isImpactSection && line.trim().startsWith("-")) {
                            PriceRecommendationResponse.PriceImpact impact = new PriceRecommendationResponse.PriceImpact();
                            String content = line.trim().substring(1).trim();
                            impact.setDescription(content);
                            impacts.add(impact);
                        }
                    }
                    recommendation.setPriceImpacts(impacts);
                    
                    // 가격 조정 제안 추출
                    if (request.getCurrentPrice() != null) {
                        boolean isAdjustmentSection = false;
                        StringBuilder adjustment = new StringBuilder();
                        for (String line : lines) {
                            if (line.contains("4. 가격 조정 제안")) {
                                isAdjustmentSection = true;
                                continue;
                            } else if (line.contains("5. 시장 전략 제안")) {
                                isAdjustmentSection = false;
                                continue;
                            }
                            
                            if (isAdjustmentSection && line.trim().startsWith("-")) {
                                adjustment.append(line.trim().substring(1).trim()).append("\n");
                            }
                        }
                        recommendation.setPriceAdjustmentSuggestion(adjustment.toString().trim());
                    }
                    
                    // 시장 전략 추출
                    boolean isStrategySection = false;
                    StringBuilder strategy = new StringBuilder();
                    for (String line : lines) {
                        if (line.contains("5. 시장 전략 제안")) {
                            isStrategySection = true;
                            continue;
                        }
                        
                        if (isStrategySection && line.trim().startsWith("-")) {
                            strategy.append(line.trim().substring(1).trim()).append("\n");
                        }
                    }
                    recommendation.setMarketStrategy(strategy.toString().trim());
                    
                } catch (Exception e) {
                    // 파싱 실패 시 기본 응답 생성
                    recommendation.setRecommendedMinPrice(BigDecimal.ZERO);
                    recommendation.setRecommendedMaxPrice(BigDecimal.ZERO);
                    recommendation.setRecommendedAveragePrice(BigDecimal.ZERO);
                    recommendation.setPriceFactors(List.of("AI 응답 파싱에 실패했습니다."));
                }
                
                return recommendation;
            });
    }

    /**
     * 상품 목록의 평균 가격 계산
     */
    private BigDecimal calculateAveragePrice(List<Product> products) {
        return products.stream()
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(products.size()), 0, RoundingMode.HALF_UP);
    }

    /**
     * 상품 목록의 최소 가격 찾기
     */
    private BigDecimal findMinPrice(List<Product> products) {
        return products.stream()
            .map(Product::getPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    /**
     * 상품 목록의 최대 가격 찾기
     */
    private BigDecimal findMaxPrice(List<Product> products) {
        return products.stream()
            .map(Product::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }
} 