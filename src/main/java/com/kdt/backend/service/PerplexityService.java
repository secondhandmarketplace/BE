package com.kdt.backend.service;

import com.kdt.backend.dto.PriceFactor;
import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceRecommendationResponse;
import com.kdt.backend.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerplexityService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    private final PromptService promptService;

    // 가격 추출을 위한 정규표현식 패턴
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:,\\d+)*)원?");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile("(?:최소|최저)\\s*(?:가격)?[:\\s]*(\\d+(?:,\\d+)*)원?");
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile("(?:최대|최고)\\s*(?:가격)?[:\\s]*(\\d+(?:,\\d+)*)원?");
    private static final Pattern AVG_PRICE_PATTERN = Pattern.compile("(?:평균|적정)\\s*(?:가격)?[:\\s]*(\\d+(?:,\\d+)*)원?");

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * [1] 가격 추천 메서드 (기존)
     */
    public Mono<PriceRecommendationResponse> getPriceRecommendation(PriceRecommendationRequest request) {
        try {
            log.info("가격 추천 요청: {}", request.getUserRequest());

            String prompt = promptService.generatePricePrompt(request);
            Map<String, Object> requestBody = createRequestBody(prompt, 0.3, 800);

            return getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(response -> parsePriceRecommendationResponse(response, request))
                    .doOnNext(result -> log.info("가격 추천 완료: {}원 ~ {}원",
                            result.getRecommendedMinPrice(), result.getRecommendedMaxPrice()))
                    .onErrorResume(e -> {
                        log.error("가격 추천 API 호출 오류: {}", e.getMessage());
                        return Mono.just(errorPriceResponse("AI API 호출 오류: " + e.getMessage()));
                    });
        } catch (Exception e) {
            log.error("가격 추천 전체 처리 오류: {}", e.getMessage());
            return Mono.just(errorPriceResponse("전체 처리 오류: " + e.getMessage()));
        }
    }

    /**
     * [2] 상품 추천 메서드 (AI 기반)
     */
    public Mono<RecommendationResponse> getRecommendations(String userRequest) {
        try {
            log.info("상품 추천 요청: {}", userRequest);

            String prompt = promptService.generateRecommendationPrompt(userRequest);
            Map<String, Object> requestBody = createRequestBody(prompt, 0.7, 800);

            return getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(response -> parseRecommendationResponse(response))
                    .doOnNext(result -> log.info("상품 추천 완료: {}", result.getContent()))
                    .onErrorResume(e -> {
                        log.error("상품 추천 API 호출 오류: {}", e.getMessage());
                        return Mono.just(errorRecommendationResponse("AI API 호출 오류: " + e.getMessage()));
                    });
        } catch (Exception e) {
            log.error("상품 추천 전체 처리 오류: {}", e.getMessage());
            return Mono.just(errorRecommendationResponse("전체 처리 오류: " + e.getMessage()));
        }
    }

    /**
     * [3] 채팅 메서드 (PromptService에서 사용)
     */
    public Mono<String> chat(String prompt) {
        try {
            log.info("채팅 요청: {}", prompt.substring(0, Math.min(100, prompt.length())));

            Map<String, Object> requestBody = createRequestBody(prompt, 0.5, 600);

            return getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(this::extractTextFromResponse)
                    .doOnNext(result -> log.info("채팅 응답 완료"))
                    .onErrorResume(e -> {
                        log.error("채팅 API 호출 오류: {}", e.getMessage());
                        return Mono.just("AI 응답을 받을 수 없습니다: " + e.getMessage());
                    });
        } catch (Exception e) {
            log.error("채팅 전체 처리 오류: {}", e.getMessage());
            return Mono.just("처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 공통 요청 본문 생성
     */
    private Map<String, Object> createRequestBody(String prompt, double temperature, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-sonar-small-128k-online");

        List<Map<String, String>> messages = List.of(Map.of(
                "role", "user",
                "content", prompt
        ));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        return requestBody;
    }

    /**
     * [4] 가격 추천 응답 파싱 (개선됨)
     */
    private PriceRecommendationResponse parsePriceRecommendationResponse(
            Map<String, Object> response, PriceRecommendationRequest request) {
        try {
            String content = extractTextFromResponse(response);

            if (content == null || content.trim().isEmpty()) {
                return errorPriceResponse("AI 응답이 비어있습니다.");
            }

            PriceRecommendationResponse dto = parseContentToPriceDto(content, request);
            dto.setOriginalResponse(content);

            return dto;
        } catch (Exception e) {
            log.error("가격 추천 응답 파싱 오류: {}", e.getMessage());
            return errorPriceResponse("AI 응답 파싱 오류: " + e.getMessage());
        }
    }

    /**
     * [5] 상품 추천 응답 파싱 (개선됨)
     */
    private RecommendationResponse parseRecommendationResponse(Map<String, Object> response) {
        try {
            String content = extractTextFromResponse(response);

            if (content == null || content.trim().isEmpty()) {
                return errorRecommendationResponse("AI 응답이 비어있습니다.");
            }

            // ✅ 사용자 선호사항 반영: 최근 등록순 정렬 정보 포함
            RecommendationResponse dto = new RecommendationResponse(content);
            dto.setSortedBy("latest"); // 최근 등록순 기본값
            dto.setTimestamp(LocalDateTime.now());
            dto.setSuccess(true);

            return dto;
        } catch (Exception e) {
            log.error("상품 추천 응답 파싱 오류: {}", e.getMessage());
            return errorRecommendationResponse("AI 응답 파싱 오류: " + e.getMessage());
        }
    }

    /**
     * 응답에서 텍스트 추출
     */
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
            return null;
        } catch (Exception e) {
            log.error("응답 텍스트 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * [6] 가격 추천 내용 파싱 (정규표현식 사용)
     */
    private PriceRecommendationResponse parseContentToPriceDto(String content, PriceRecommendationRequest request) {
        PriceRecommendationResponse dto = new PriceRecommendationResponse();

        try {
            // 가격 정보 추출
            BigDecimal minPrice = extractPrice(content, MIN_PRICE_PATTERN);
            BigDecimal maxPrice = extractPrice(content, MAX_PRICE_PATTERN);
            BigDecimal avgPrice = extractPrice(content, AVG_PRICE_PATTERN);

            // 추출된 가격이 없으면 기본값 설정
            if (minPrice == null && maxPrice == null && avgPrice == null) {
                // 요청 상품에 따른 기본 가격 설정
                if (request.getUserRequest().toLowerCase().contains("아이폰")) {
                    minPrice = new BigDecimal("500000");
                    maxPrice = new BigDecimal("800000");
                    avgPrice = new BigDecimal("650000");
                } else {
                    minPrice = new BigDecimal("50000");
                    maxPrice = new BigDecimal("150000");
                    avgPrice = new BigDecimal("100000");
                }
            } else {
                // 누락된 가격 보완
                if (avgPrice != null) {
                    if (minPrice == null) minPrice = avgPrice.multiply(new BigDecimal("0.8"));
                    if (maxPrice == null) maxPrice = avgPrice.multiply(new BigDecimal("1.2"));
                } else if (minPrice != null && maxPrice != null) {
                    avgPrice = minPrice.add(maxPrice).divide(new BigDecimal("2"));
                }
            }

            dto.setRecommendedMinPrice(minPrice);
            dto.setRecommendedMaxPrice(maxPrice);
            dto.setRecommendedAveragePrice(avgPrice);

            // 가격 결정 요소 추출
            dto.setPriceFactors(extractPriceFactors(content));

            // 가격 영향도 설정
            dto.setPriceImpacts(createPriceImpacts());

            // 조정 제안 및 전략
            dto.setPriceAdjustmentSuggestion(extractAdjustmentSuggestion(content));
            dto.setMarketStrategy(extractMarketStrategy(content));

        } catch (Exception e) {
            log.error("가격 정보 파싱 중 오류: {}", e.getMessage());
            // 기본값 설정
            dto.setRecommendedMinPrice(new BigDecimal("50000"));
            dto.setRecommendedMaxPrice(new BigDecimal("150000"));
            dto.setRecommendedAveragePrice(new BigDecimal("100000"));
            dto.setPriceFactors(List.of("AI 분석 결과"));
        }

        return dto;
    }

    /**
     * 정규표현식으로 가격 추출
     */
    private BigDecimal extractPrice(String content, Pattern pattern) {
        try {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String priceStr = matcher.group(1).replace(",", "");
                return new BigDecimal(priceStr);
            }
        } catch (Exception e) {
            log.debug("가격 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 가격 결정 요소 추출
     */
    private List<String> extractPriceFactors(String content) {
        List<String> factors = new ArrayList<>();

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("요소") || line.contains("근거") || line.contains("영향")) {
                if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                    factors.add(line.trim().substring(1).trim());
                }
            }
        }

        if (factors.isEmpty()) {
            factors.add("상품 상태 및 시장 수요");
            factors.add("브랜드 인기도");
            factors.add("최근 거래 사례");
        }

        return factors;
    }

    /**
     * 가격 영향도 생성
     */
    private List<PriceRecommendationResponse.PriceImpact> createPriceImpacts() {
        List<PriceRecommendationResponse.PriceImpact> impacts = new ArrayList<>();

        PriceRecommendationResponse.PriceImpact impact1 = new PriceRecommendationResponse.PriceImpact();
        impact1.setFactorName("상품 상태");
        impact1.setImpact(new BigDecimal("0.3"));
        impact1.setDescription("상품의 외관과 기능 상태");
        impacts.add(impact1);

        PriceRecommendationResponse.PriceImpact impact2 = new PriceRecommendationResponse.PriceImpact();
        impact2.setFactorName("시장 수요");
        impact2.setImpact(new BigDecimal("0.25"));
        impact2.setDescription("해당 상품의 시장 인기도");
        impacts.add(impact2);

        return impacts;
    }

    /**
     * 조정 제안 추출
     */
    private String extractAdjustmentSuggestion(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("조정") || line.contains("권장") || line.contains("제안")) {
                return line.trim();
            }
        }
        return "시장 상황을 고려하여 적정 가격대로 설정하시기 바랍니다.";
    }

    /**
     * 시장 전략 추출
     */
    private String extractMarketStrategy(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("전략") || line.contains("방법") || line.contains("팁")) {
                return line.trim();
            }
        }
        return "비슷한 상품의 최근 거래가와 비교하여 경쟁력 있는 가격을 설정하세요.";
    }

    /**
     * 에러 응답 생성
     */
    private PriceRecommendationResponse errorPriceResponse(String message) {
        PriceRecommendationResponse dto = new PriceRecommendationResponse();
        dto.setOriginalResponse(message);
        dto.setRecommendedMinPrice(BigDecimal.ZERO);
        dto.setRecommendedMaxPrice(BigDecimal.ZERO);
        dto.setRecommendedAveragePrice(BigDecimal.ZERO);
        dto.setPriceFactors(List.of("오류로 인한 기본 응답"));
        return dto;
    }

    private RecommendationResponse errorRecommendationResponse(String message) {
        RecommendationResponse response = new RecommendationResponse(message);
        response.setSuccess(false);
        response.setSortedBy("latest"); // ✅ 사용자 선호사항 반영
        return response;
    }
}
