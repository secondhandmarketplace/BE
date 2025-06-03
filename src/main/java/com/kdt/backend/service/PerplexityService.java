package com.kdt.backend.service;

import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceRecommendationResponse;
import com.kdt.backend.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PerplexityService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    private final PromptService promptService;

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // [1] 기존 가격 추천 메서드
    public Mono<PriceRecommendationResponse> getPriceRecommendation(PriceRecommendationRequest request) {
        try {
            String prompt = promptService.generatePricePrompt(request);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-sonar-small-128k-online");
            List<Map<String, String>> messages = List.of(Map.of(
                    "role", "user",
                    "content", prompt
            ));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.3);

            return getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(response -> parsePriceRecommendationResponse(response, prompt))
                    .onErrorResume(e -> Mono.just(errorPriceResponse("AI API 호출 오류: " + e.getMessage())));
        } catch (Exception e) {
            return Mono.just(errorPriceResponse("전체 처리 오류: " + e.getMessage()));
        }
    }

    // [2] 상품 추천(질문 답변) 메서드 추가
    public Mono<RecommendationResponse> getRecommendations(String userRequest) {
        try {
            String prompt = promptService.generateRecommendationPrompt(userRequest);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-sonar-small-128k-online");
            List<Map<String, String>> messages = List.of(Map.of(
                    "role", "user",
                    "content", prompt
            ));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.7);

            return getWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(response -> parseRecommendationResponse(response, prompt))
                    .onErrorResume(e -> Mono.just(errorRecommendationResponse("AI API 호출 오류: " + e.getMessage())));
        } catch (Exception e) {
            return Mono.just(errorRecommendationResponse("전체 처리 오류: " + e.getMessage()));
        }
    }

    // [3] 가격 추천 응답 파싱
    private PriceRecommendationResponse parsePriceRecommendationResponse(Map<String, Object> response, String prompt) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                PriceRecommendationResponse dto = parseContentToPriceDto(content);
                dto.setOriginalResponse(content);
                return dto;
            }
            return errorPriceResponse("AI 응답이 비어있습니다.");
        } catch (Exception e) {
            return errorPriceResponse("AI 응답 파싱 오류: " + e.getMessage());
        }
    }

    // [4] 상품 추천 응답 파싱
    private RecommendationResponse parseRecommendationResponse(Map<String, Object> response, String prompt) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                return new RecommendationResponse(content != null ? content : "응답이 없습니다.");
            }
            return new RecommendationResponse("AI 응답이 비어있습니다.");
        } catch (Exception e) {
            return new RecommendationResponse("AI 응답 파싱 오류: " + e.getMessage());
        }
    }

    private PriceRecommendationResponse parseContentToPriceDto(String content) {
        PriceRecommendationResponse dto = new PriceRecommendationResponse();
        dto.setRecommendedMinPrice(new BigDecimal("50000"));
        dto.setRecommendedMaxPrice(new BigDecimal("90000"));
        dto.setRecommendedAveragePrice(new BigDecimal("70000"));
        dto.setPriceFactors(List.of("상품 상태", "시장 수요", "브랜드 인기"));
        dto.setPriceAdjustmentSuggestion("현재 가격이 평균보다 높습니다. 7만원대로 조정 권장.");
        dto.setMarketStrategy("비슷한 상품의 최근 거래가와 비교해 가격을 설정하세요.");
        dto.setOriginalResponse(content);
        return dto;
    }

    private PriceRecommendationResponse errorPriceResponse(String message) {
        PriceRecommendationResponse dto = new PriceRecommendationResponse();
        dto.setOriginalResponse(message);
        return dto;
    }

    private RecommendationResponse errorRecommendationResponse(String message) {
        return new RecommendationResponse(message);
    }
}
