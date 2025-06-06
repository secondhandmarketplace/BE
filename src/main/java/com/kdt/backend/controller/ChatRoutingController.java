package com.kdt.backend.controller;

import com.kdt.backend.service.PromptService;
import com.kdt.backend.service.PriceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat-routing")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class ChatRoutingController {

    private final PromptService promptService;
    private final PriceRecommendationService priceRecommendationService;

    /**
     * ✅ 통합 채팅 엔드포인트 (의도별 라우팅)
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> handleChatMessage(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        try {
            log.info("통합 채팅 요청: {}", userMessage);

            // ✅ 1순위: 인사 메시지 처리 (상품 추천 없이 인사만)
            if (isGreetingMessage(userMessage)) {
                return handleGreetingOnly(userMessage);
            }

            // ✅ 2순위: 작별 인사 처리 (상품 추천 없이 작별만)
            if (isFarewellMessage(userMessage)) {
                return handleFarewellOnly(userMessage);
            }

            // ✅ 3순위: 가격 추천 요청
            if (isPriceRecommendationRequest(userMessage)) {
                return forwardToPriceRecommendation(userMessage);
            }

            // ✅ 4순위: 상품 검색/추천 요청
            if (isProductRelatedRequest(userMessage)) {
                return forwardToProductRecommendation(userMessage);
            }

            // ✅ 5순위: 일반 대화 (도움말, 질문 등)
            return handleGeneralConversation(userMessage);

        } catch (Exception e) {
            log.error("채팅 처리 실패: {}", e.getMessage());
            return createErrorResponse("죄송해요, 요청을 처리하는 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 인사 전용 처리 (상품 추천 없음)
     */
    private ResponseEntity<Map<String, Object>> handleGreetingOnly(String userMessage) {
        String greetingResponse = "안녕하세요! 😊 중고거래 플랫폼에 오신 것을 환영합니다.\n\n" +
                "저는 다음과 같은 역할을 할 수 있으며, 무엇을 도와드릴까요?\n\n" +
                "🔍 **상품 검색 및 추천**\n" +
                "- 원하시는 상품을 찾아드려요\n" +
                "- 카테고리별 상품 추천\n" +
                "- 최신 등록 상품 안내\n\n" +
                "💰 **가격 추천 및 시세 조회**\n" +
                "- 판매하려는 상품의 적정 가격 추천\n" +
                "- 시장 가격 분석\n" +
                "- 가격 조정 제안\n\n" +
                "💬 **일반 문의 및 도움말**\n" +
                "- 플랫폼 사용법 안내\n" +
                "- 거래 관련 질문 답변\n" +
                "- 기타 궁금한 점 해결\n\n" +
                "무엇을 도와드릴까요? 😄";

        Map<String, Object> result = new HashMap<>();
        result.put("response", greetingResponse);
        result.put("type", "GREETING_ONLY");
        result.put("sortedBy", "latest"); // 사용자 선호사항 [2] 반영
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        log.info("인사 전용 응답 완료");
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 작별 인사 전용 처리 (상품 추천 없음)
     */
    private ResponseEntity<Map<String, Object>> handleFarewellOnly(String userMessage) {
        String farewellResponse = generateFarewellResponse(userMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("response", farewellResponse);
        result.put("type", "FAREWELL_ONLY");
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        log.info("작별 인사 전용 응답 완료");
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 가격 추천으로 포워딩
     */
    private ResponseEntity<Map<String, Object>> forwardToPriceRecommendation(String userMessage) {
        try {
            log.info("가격 추천으로 포워딩: {}", userMessage);
            String response = priceRecommendationService.getDirectPriceRecommendation(userMessage);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("type", "PRICE_RECOMMENDATION");
            result.put("forwardedTo", "PriceRecommendationService");
            result.put("sortedBy", "latest"); // 사용자 선호사항 [2] 반영
            result.put("timestamp", LocalDateTime.now());
            result.put("success", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return createErrorResponse("가격 추천 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 상품 추천으로 포워딩
     */
    private ResponseEntity<Map<String, Object>> forwardToProductRecommendation(String userMessage) {
        try {
            log.info("상품 추천으로 포워딩: {}", userMessage);
            String response = promptService.generateRecommendationResponse(userMessage);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("type", "PRODUCT_RECOMMENDATION");
            result.put("forwardedTo", "PromptService");
            result.put("sortedBy", "latest"); // 사용자 선호사항 [2] 반영
            result.put("timestamp", LocalDateTime.now());
            result.put("success", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return createErrorResponse("상품 추천 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 일반 대화 처리
     */
    private ResponseEntity<Map<String, Object>> handleGeneralConversation(String userMessage) {
        String conversationResponse = generateGeneralConversationResponse(userMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("response", conversationResponse);
        result.put("type", "GENERAL_CONVERSATION");
        result.put("sortedBy", "latest"); // 사용자 선호사항 [2] 반영
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    // ===== 의도 분석 메서드들 (강화된 버전) =====

    /**
     * ✅ 인사 메시지 판별 (정확한 매칭)
     */
    private boolean isGreetingMessage(String message) {
        String[] exactGreetings = {
                "안녕", "안녕하세요", "hello", "hi", "하이", "헬로", "반갑습니다"
        };
        String trimmed = message.trim().toLowerCase();

        // 정확한 인사말만 매칭 (다른 내용이 포함되면 제외)
        for (String greeting : exactGreetings) {
            if (trimmed.equals(greeting) ||
                    (trimmed.startsWith(greeting) && trimmed.length() <= greeting.length() + 3)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ✅ 작별 인사 메시지 판별 (정확한 매칭)
     */
    private boolean isFarewellMessage(String message) {
        String[] exactFarewells = {
                "잘자", "잘가", "잘 가", "bye", "goodbye", "잘 있어", "안녕"
        };
        String trimmed = message.trim().toLowerCase();

        for (String farewell : exactFarewells) {
            if (trimmed.equals(farewell) ||
                    (trimmed.startsWith(farewell) && trimmed.length() <= farewell.length() + 3)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ✅ 가격 추천 요청 판별
     */
    private boolean isPriceRecommendationRequest(String message) {
        String[] priceKeywords = {
                "얼마에 팔", "가격", "얼마", "시세", "적정가", "판매가", "팔까", "가격 추천"
        };
        String lower = message.toLowerCase();

        for (String keyword : priceKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ✅ 상품 관련 요청 판별
     */
    private boolean isProductRelatedRequest(String message) {
        String[] productKeywords = {
                "찾아줘", "검색", "보여줘", "추천", "상품", "아이템", "물건",
                "전자기기", "의류", "도서", "가구", "최신", "최저가", "최고가"
        };
        String lower = message.toLowerCase();

        for (String keyword : productKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ===== 응답 생성 메서드들 =====

    private String generateFarewellResponse(String message) {
        String lower = message.toLowerCase().trim();

        if (lower.contains("잘자")) {
            return "잘 자요! 🌙 좋은 꿈 꾸세요!";
        } else if (lower.contains("잘가") || lower.contains("잘 가")) {
            return "안녕히 가세요! 👋 또 만나요!";
        } else if (lower.contains("bye") || lower.contains("goodbye")) {
            return "Goodbye! 👋 See you later!";
        } else {
            return "안녕히 가세요! 😊 또 언제든 찾아주세요!";
        }
    }

    private String generateGeneralConversationResponse(String message) {
        return "궁금한 점이 있으시군요! 😊\n\n" +
                "구체적으로 무엇을 도와드릴까요?\n" +
                "• 상품 검색: \"아이폰 찾아줘\"\n" +
                "• 가격 문의: \"아이폰 얼마에 팔까?\"\n" +
                "• 도움말: \"도움말\" 또는 \"사용법\"\n\n" +
                "편하게 말씀해주세요! 🤗";
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, String error) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("response", message);
        errorResult.put("error", error);
        errorResult.put("success", false);
        errorResult.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(500).body(errorResult);
    }

    /**
     * ✅ 서비스 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getChatStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "unified-chat-routing");
        status.put("status", "active");
        status.put("purpose", "의도별 스마트 라우팅");
        status.put("sortedBy", "latest"); // 사용자 선호사항 [2] 반영
        status.put("timestamp", LocalDateTime.now());
        status.put("routingOrder", new String[]{
                "1. GREETING_ONLY", "2. FAREWELL_ONLY", "3. PRICE_RECOMMENDATION",
                "4. PRODUCT_RECOMMENDATION", "5. GENERAL_CONVERSATION"
        });
        return ResponseEntity.ok(status);
    }
}
