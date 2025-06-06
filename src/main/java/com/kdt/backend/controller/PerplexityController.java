package com.kdt.backend.controller;

import com.kdt.backend.service.PerplexityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perplexity")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class PerplexityController {

    private final PerplexityService perplexityService;

    /**
     * ✅ 유연한 대화 처리 (인사, 일반 대화, 도움말)
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> handleFlexibleChat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        try {
            log.info("유연한 대화 요청: {}", userMessage);

            if (isGreetingMessage(userMessage)) {
                return handleGreeting(userMessage);
            } else if (isHelpRequest(userMessage)) {
                return handleHelpRequest(userMessage);
            } else if (isFarewellMessage(userMessage)) {
                return handleFarewell(userMessage);
            } else if (isGeneralQuestion(userMessage)) {
                return handleGeneralQuestion(userMessage);
            } else {
                // AI 기반 일반 대화
                return handleAIConversation(userMessage);
            }

        } catch (Exception e) {
            log.error("유연한 대화 처리 실패: {}", e.getMessage());
            return createErrorResponse("죄송해요, 요청을 처리하는 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 인사 처리 (환영 메시지 + 역할 안내)
     */
    private ResponseEntity<Map<String, Object>> handleGreeting(String userMessage) {
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
        result.put("type", "GREETING_WITH_GUIDE");
        result.put("sortedBy", "latest"); // 사용자 선호사항 [1] 반영
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 도움말 요청 처리
     */
    private ResponseEntity<Map<String, Object>> handleHelpRequest(String userMessage) {
        String helpResponse = "도움이 필요하시군요! 😊 제가 도와드릴 수 있는 것들을 알려드릴게요:\n\n" +
                "📋 **주요 기능**\n" +
                "• `/api/recommendations/chat` - 상품 추천 및 검색\n" +
                "• `/api/v1/price-recommendations/direct` - 가격 추천\n" +
                "• `/api/perplexity/chat` - 일반 대화 및 문의\n\n" +
                "💡 **사용 예시**\n" +
                "• \"아이폰 찾아줘\" - 상품 검색\n" +
                "• \"아이폰 12 A급 얼마에 팔까?\" - 가격 추천\n" +
                "• \"전자기기 보여줘\" - 카테고리별 추천\n" +
                "• \"최신 상품 뭐 있어?\" - 최근 등록 상품 조회\n\n" +
                "🔄 **정렬 기준**: 모든 상품은 최근 등록순으로 정렬됩니다\n\n" +
                "더 궁금한 점이 있으시면 언제든 말씀해주세요! 🤗";

        Map<String, Object> result = new HashMap<>();
        result.put("response", helpResponse);
        result.put("type", "HELP_GUIDE");
        result.put("sortedBy", "latest"); // 사용자 선호사항 [1] 반영
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 작별 인사 처리
     */
    private ResponseEntity<Map<String, Object>> handleFarewell(String userMessage) {
        String farewellResponse = generateFarewellResponse(userMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("response", farewellResponse);
        result.put("type", "FAREWELL");
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 일반 질문 처리
     */
    private ResponseEntity<Map<String, Object>> handleGeneralQuestion(String userMessage) {
        String questionResponse = generateGeneralQuestionResponse(userMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("response", questionResponse);
        result.put("type", "GENERAL_QUESTION");
        result.put("sortedBy", "latest"); // 사용자 선호사항 [1] 반영
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ AI 기반 일반 대화
     */
    private ResponseEntity<Map<String, Object>> handleAIConversation(String userMessage) {
        try {
            // PerplexityService를 통한 AI 대화
            return perplexityService.chat(userMessage)
                    .map(aiResponse -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("response", aiResponse);
                        result.put("type", "AI_CONVERSATION");
                        result.put("dataSource", "PERPLEXITY_AI");
                        result.put("sortedBy", "latest"); // 사용자 선호사항 [1] 반영
                        result.put("timestamp", LocalDateTime.now());
                        result.put("success", true);
                        return ResponseEntity.ok(result);
                    })
                    .block(); // Reactive to blocking conversion for REST controller
        } catch (Exception e) {
            log.error("AI 대화 실패: {}", e.getMessage());
            return createErrorResponse("AI 대화 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    // ===== 의도 분석 메서드들 =====

    private boolean isGreetingMessage(String message) {
        String[] greetings = {
                "안녕", "hello", "hi", "안녕하세요", "반갑습니다", "처음", "하이", "dkssud"
        };
        String lower = message.toLowerCase().trim();

        for (String greeting : greetings) {
            if (lower.equals(greeting) || lower.startsWith(greeting)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHelpRequest(String message) {
        String[] helpKeywords = {
                "도움", "help", "사용법", "어떻게", "뭘 할 수", "기능", "역할", "할 수 있는"
        };
        String lower = message.toLowerCase();

        for (String keyword : helpKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFarewellMessage(String message) {
        String[] farewells = {
                "잘자", "잘가", "bye", "goodbye", "잘 있어", "나중에 봐", "또 봐"
        };
        String lower = message.toLowerCase().trim();

        for (String farewell : farewells) {
            if (lower.equals(farewell) || lower.startsWith(farewell)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGeneralQuestion(String message) {
        String[] questionWords = {
                "뭐", "무엇", "어디", "언제", "왜", "어떻게", "누구", "얼마나"
        };
        String lower = message.toLowerCase();

        for (String word : questionWords) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // ===== 응답 생성 메서드들 =====

    private String generateFarewellResponse(String message) {
        String lower = message.toLowerCase().trim();

        if (lower.contains("잘자")) {
            return "잘 자요! 🌙 좋은 꿈 꾸세요! 내일도 좋은 거래 되시길 바라요!";
        } else if (lower.contains("잘가") || lower.contains("잘 있어")) {
            return "안녕히 가세요! 👋 또 만나요! 좋은 거래 되시길 바라요!";
        } else if (lower.contains("bye") || lower.contains("goodbye")) {
            return "Goodbye! 👋 See you later! Have a great day!";
        } else {
            return "안녕히 가세요! 😊 또 언제든 찾아주세요!";
        }
    }

    private String generateGeneralQuestionResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("뭐") && (lower.contains("있어") || lower.contains("해"))) {
            return "다양한 것들을 도와드릴 수 있어요! 😊\n\n" +
                    "• 상품 검색 및 추천\n" +
                    "• 가격 추천 및 시세 조회\n" +
                    "• 플랫폼 사용법 안내\n" +
                    "• 일반적인 질문 답변\n\n" +
                    "구체적으로 무엇을 도와드릴까요?";
        } else if (lower.contains("어떻게")) {
            return "사용법이 궁금하시군요! 📖\n\n" +
                    "간단히 말씀해주시면 됩니다:\n" +
                    "• \"아이폰 찾아줘\" - 상품 검색\n" +
                    "• \"얼마에 팔까?\" - 가격 추천\n" +
                    "• \"도움말\" - 자세한 사용법\n\n" +
                    "무엇을 시작해볼까요?";
        } else {
            return "궁금한 점이 있으시군요! 🤔\n\n" +
                    "더 구체적으로 말씀해주시면 정확한 답변을 드릴 수 있어요.\n" +
                    "상품 관련 문의, 가격 문의, 사용법 문의 등 무엇이든 편하게 물어보세요! 😊";
        }
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
    public ResponseEntity<Map<String, Object>> getPerplexityStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "perplexity-flexible-chat");
        status.put("status", "active");
        status.put("purpose", "유연한 대화 및 일반 문의 처리");
        status.put("sortedBy", "latest"); // 사용자 선호사항 [1] 반영
        status.put("timestamp", LocalDateTime.now());
        status.put("supportedIntents", new String[]{
                "GREETING_WITH_GUIDE", "HELP_GUIDE", "FAREWELL",
                "GENERAL_QUESTION", "AI_CONVERSATION"
        });
        status.put("features", new String[]{
                "환영 메시지 + 역할 안내", "도움말 제공", "일반 대화", "AI 기반 응답"
        });
        return ResponseEntity.ok(status);
    }
}
