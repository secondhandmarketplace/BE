package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.config.PerplexityConfig;
import ac.su.kdt.secondhandmarketplace.dto.*;
import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRecommendationService {
    
    private final ProductRepository productRepository;
    private final PerplexityService perplexityService;
    private final PromptService promptService;

    // 가격 추출을 위한 정규표현식 패턴
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:,\\d+)*)");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile("(?:최소\\s*가격|최소가격)[^\\d]*(\\d+(?:,\\d+)*)");
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile("(?:최대\\s*가격|최대가격)[^\\d]*(\\d+(?:,\\d+)*)");
    private static final Pattern AVG_PRICE_PATTERN = Pattern.compile("(?:평균\\s*가격|평균가격)[^\\d]*(\\d+(?:,\\d+)*)");

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
        
        try {
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
            
            if (factors.isEmpty()) {
                PriceFactor noDataFactor = new PriceFactor();
                noDataFactor.setDataSource("INTERNAL");
                noDataFactor.setFactorName("내부_데이터_없음");
                noDataFactor.setDescription("내부 플랫폼에서 유사한 상품 데이터를 찾을 수 없습니다.");
                factors.add(noDataFactor);
            }
            
        } catch (Exception e) {
            log.error("내부 데이터 분석 중 오류 발생: {}", e.getMessage());
            PriceFactor errorFactor = new PriceFactor();
            errorFactor.setDataSource("INTERNAL");
            errorFactor.setFactorName("내부_데이터_오류");
            errorFactor.setDescription("내부 플랫폼 데이터 조회에 실패했습니다: " + e.getMessage());
            factors.add(errorFactor);
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
        
        try {
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
            
        } catch (Exception e) {
            log.error("외부 데이터 분석 중 오류 발생: {}", e.getMessage());
            PriceFactor errorFactor = new PriceFactor();
            errorFactor.setDataSource("EXTERNAL");
            errorFactor.setFactorName("외부_데이터_오류");
            errorFactor.setDescription("외부 시장 데이터 조회에 실패했습니다: " + e.getMessage());
            factors.add(errorFactor);
        }
        
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
        log.info("=== 가격 추천 요청 시작 ===");
        log.info("생성된 프롬프트: {}", prompt);
        
        // AI API 호출
        return perplexityService.chat(prompt)
            .map(response -> {
                PriceRecommendationResponse recommendation = new PriceRecommendationResponse();
                
                try {
                    // 전체 응답 로깅
                    log.info("=== Perplexity API 응답 ===");
                    log.info("응답 구조: {}", response.keySet());
                    log.info("전체 응답: {}", response);
                    
                    // AI 응답에서 가격 정보 추출
                    String responseText;
                    if (response.containsKey("choices")) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (!choices.isEmpty()) {
                            Map<String, Object> choice = choices.get(0);
                            log.info("첫 번째 선택지: {}", choice);
                            
                            if (choice.containsKey("message")) {
                                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                                log.info("메시지 객체: {}", message);
                                responseText = (String) message.get("content");
                            } else if (choice.containsKey("text")) {
                                responseText = (String) choice.get("text");
                            } else {
                                throw new RuntimeException("응답에서 메시지 내용을 찾을 수 없습니다.");
                            }
                        } else {
                            throw new RuntimeException("응답에 선택지가 없습니다.");
                        }
                    } else if (response.containsKey("text")) {
                        responseText = (String) response.get("text");
                    } else {
                        throw new RuntimeException("응답 형식이 예상과 다릅니다.");
                    }
                    
                    log.info("=== 추출된 AI 응답 ===");
                    log.info("응답 내용: {}", responseText);
                    
                    // 원본 응답 저장
                    recommendation.setOriginalResponse(responseText);
                    log.info("저장된 원본 응답: {}", recommendation.getOriginalResponse());
                    
                    // 가격 정보 추출
                    extractPriceInfo(responseText, recommendation);
                    
                    // 가격 결정 근거 추출
                    extractPriceFactors(responseText, recommendation);
                    
                    // 가격 영향도 추출
                    extractPriceImpacts(responseText, recommendation);
                    
                    // 가격 조정 제안 추출
                    if (request.getCurrentPrice() != null) {
                        extractPriceAdjustment(responseText, recommendation);
                    }
                    
                    // 시장 전략 추출
                    extractMarketStrategy(responseText, recommendation);
                    
                    // 추출된 정보 검증
                    validateRecommendation(recommendation);
                    
                    // 최종 응답 로깅
                    log.info("=== 최종 응답 객체 ===");
                    log.info("추천 가격 범위: {}원 ~ {}원", 
                        recommendation.getRecommendedMinPrice(),
                        recommendation.getRecommendedMaxPrice());
                    log.info("평균 추천 가격: {}원", recommendation.getRecommendedAveragePrice());
                    log.info("가격 결정 요소: {}", recommendation.getPriceFactors());
                    log.info("가격 영향도: {}", recommendation.getPriceImpacts());
                    
                } catch (Exception e) {
                    log.error("=== 가격 추천 파싱 오류 ===");
                    log.error("오류 메시지: {}", e.getMessage());
                    log.error("오류 상세: ", e);
                    handleParsingError(recommendation, e);
                }
                
                return recommendation;
            });
    }

    /**
     * 가격 정보 추출
     */
    private void extractPriceInfo(String responseText, PriceRecommendationResponse recommendation) {
        try {
            // 최소 가격 추출
            Matcher minMatcher = Pattern.compile("최소\\s*가격:\\s*(\\d+(?:,\\d+)*)").matcher(responseText);
            if (minMatcher.find()) {
                String minPriceStr = minMatcher.group(1).replace(",", "");
                recommendation.setRecommendedMinPrice(new BigDecimal(minPriceStr));
            }

            // 최대 가격 추출
            Matcher maxMatcher = Pattern.compile("최대\\s*가격:\\s*(\\d+(?:,\\d+)*)").matcher(responseText);
            if (maxMatcher.find()) {
                String maxPriceStr = maxMatcher.group(1).replace(",", "");
                recommendation.setRecommendedMaxPrice(new BigDecimal(maxPriceStr));
            }

            // 평균 가격 추출
            Matcher avgMatcher = Pattern.compile("평균\\s*가격:\\s*(\\d+(?:,\\d+)*)").matcher(responseText);
            if (avgMatcher.find()) {
                String avgPriceStr = avgMatcher.group(1).replace(",", "");
                recommendation.setRecommendedAveragePrice(new BigDecimal(avgPriceStr));
            }

            // 가격이 추출되지 않은 경우 실패 메시지 설정
            if (recommendation.getRecommendedMinPrice() == null || 
                recommendation.getRecommendedMaxPrice() == null || 
                recommendation.getRecommendedAveragePrice() == null) {
                throw new RuntimeException("가격 정보를 추출할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("가격 정보 추출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("가격 정보 조회에 실패했습니다.");
        }
    }

    /**
     * 가격 결정 근거 추출
     */
    private void extractPriceFactors(String responseText, PriceRecommendationResponse recommendation) {
        try {
            List<String> factors = new ArrayList<>();
            String[] sections = responseText.split("##");
            
            for (String section : sections) {
                if (section.contains("가격 결정 근거") || section.contains("가격 영향 요소 분석")) {
                    String[] lines = section.split("\n");
                    for (String line : lines) {
                        if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                            String factor = line.trim().substring(1).trim();
                            if (!factor.isEmpty()) {
                                factors.add(factor);
                            }
                        }
                    }
                }
            }

            if (factors.isEmpty()) {
                throw new RuntimeException("가격 결정 요소를 추출할 수 없습니다.");
            }
            recommendation.setPriceFactors(factors);
        } catch (Exception e) {
            log.error("가격 결정 요소 추출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("가격 결정 요소 조회에 실패했습니다.");
        }
    }

    /**
     * 가격 영향도 추출
     */
    private void extractPriceImpacts(String responseText, PriceRecommendationResponse recommendation) {
        try {
            List<PriceRecommendationResponse.PriceImpact> impacts = new ArrayList<>();
            String[] sections = responseText.split("##");
            
            for (String section : sections) {
                if (section.contains("가격 영향 요소 분석")) {
                    String[] lines = section.split("\n");
                    for (String line : lines) {
                        if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                            String impactText = line.trim().substring(1).trim();
                            if (!impactText.isEmpty()) {
                                PriceRecommendationResponse.PriceImpact impact = new PriceRecommendationResponse.PriceImpact();
                                impact.setFactorName(impactText);
                                impact.setImpact(BigDecimal.ZERO);
                                impact.setDescription(impactText);
                                impacts.add(impact);
                            }
                        }
                    }
                }
            }

            if (impacts.isEmpty()) {
                throw new RuntimeException("가격 영향도를 추출할 수 없습니다.");
            }
            recommendation.setPriceImpacts(impacts);
        } catch (Exception e) {
            log.error("가격 영향도 추출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("가격 영향도 조회에 실패했습니다.");
        }
    }

    /**
     * 가격 조정 제안 추출
     */
    private void extractPriceAdjustment(String responseText, PriceRecommendationResponse recommendation) {
        StringBuilder adjustment = new StringBuilder();
        String[] sections = responseText.split("##");
        
        for (String section : sections) {
            if (section.contains("가격 책정 전략")) {
                String[] lines = section.split("\n");
                for (String line : lines) {
                    if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                        adjustment.append(line.trim().substring(1).trim()).append("\n");
                    }
                }
            }
        }
        
        recommendation.setPriceAdjustmentSuggestion(adjustment.toString().trim());
    }

    /**
     * 시장 전략 추출
     */
    private void extractMarketStrategy(String responseText, PriceRecommendationResponse recommendation) {
        StringBuilder strategy = new StringBuilder();
        String[] sections = responseText.split("##");
        
        for (String section : sections) {
            if (section.contains("시장 전략 제안")) {
                String[] lines = section.split("\n");
                for (String line : lines) {
                    if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                        strategy.append(line.trim().substring(1).trim()).append("\n");
                    }
                }
            }
        }
        
        recommendation.setMarketStrategy(strategy.toString().trim());
    }

    /**
     * 추천 정보 검증
     */
    private void validateRecommendation(PriceRecommendationResponse recommendation) {
        // 최소 가격이 최대 가격보다 큰 경우
        if (recommendation.getRecommendedMinPrice() != null && 
            recommendation.getRecommendedMaxPrice() != null &&
            recommendation.getRecommendedMinPrice().compareTo(recommendation.getRecommendedMaxPrice()) > 0) {
            BigDecimal temp = recommendation.getRecommendedMinPrice();
            recommendation.setRecommendedMinPrice(recommendation.getRecommendedMaxPrice());
            recommendation.setRecommendedMaxPrice(temp);
        }

        // 평균 가격이 범위를 벗어나는 경우
        if (recommendation.getRecommendedAveragePrice() != null) {
            if (recommendation.getRecommendedMinPrice() != null && 
                recommendation.getRecommendedAveragePrice().compareTo(recommendation.getRecommendedMinPrice()) < 0) {
                recommendation.setRecommendedAveragePrice(recommendation.getRecommendedMinPrice());
            }
            if (recommendation.getRecommendedMaxPrice() != null && 
                recommendation.getRecommendedAveragePrice().compareTo(recommendation.getRecommendedMaxPrice()) > 0) {
                recommendation.setRecommendedAveragePrice(recommendation.getRecommendedMaxPrice());
            }
        }
    }

    /**
     * 파싱 오류 처리
     */
    private void handleParsingError(PriceRecommendationResponse recommendation, Exception e) {
        log.error("=== 파싱 오류 처리 시작 ===");
        log.error("오류 메시지: {}", e.getMessage());
        
        recommendation.setRecommendedMinPrice(null);
        recommendation.setRecommendedMaxPrice(null);
        recommendation.setRecommendedAveragePrice(null);
        
        // 오류 메시지에 내부/외부 데이터 실패 여부 포함
        String errorMessage = "데이터 조회에 실패했습니다: ";
        if (e.getMessage().contains("내부")) {
            errorMessage += "내부 플랫폼 데이터 조회 실패";
        } else if (e.getMessage().contains("외부")) {
            errorMessage += "외부 시장 데이터 조회 실패";
        } else {
            errorMessage += e.getMessage();
        }
        
        recommendation.setPriceFactors(List.of(errorMessage));
        recommendation.setPriceImpacts(null);
        recommendation.setPriceAdjustmentSuggestion(null);
        recommendation.setMarketStrategy(null);
        
        // 원본 응답이 있는 경우 유지
        if (recommendation.getOriginalResponse() == null) {
            recommendation.setOriginalResponse("AI 응답을 받지 못했습니다: " + e.getMessage());
        }
        
        log.info("=== 파싱 오류 처리 후 응답 객체 ===");
        log.info("오류 메시지: {}", errorMessage);
        log.info("원본 응답: {}", recommendation.getOriginalResponse());
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