package com.kdt.backend.controller;

import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.entity.Item;
import com.kdt.backend.service.PromptService;
import com.kdt.backend.service.PriceRecommendationService;
import com.kdt.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final PromptService promptService;
    private final PriceRecommendationService priceRecommendationService;
    private final ItemRepository itemRepository; // ✅ 추가

    /**
     * ✅ 수정된 채팅 엔드포인트 (인사 우선 처리)
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> getChatRecommendation(@RequestBody Map<String, String> request) {
        String userRequest = request.get("message");

        try {
            log.info("요청 분석: '{}'", userRequest);

            // ✅ 1순위: 정확한 인사 메시지 체크 (가장 먼저 처리)
            if (isExactGreetingMessage(userRequest)) {
                log.info("인사 메시지로 판별: {}", userRequest);
                return handleGreetingOnly(userRequest);
            }

            // ✅ 2순위: 정확한 작별 인사 체크
            if (isExactFarewellMessage(userRequest)) {
                log.info("작별 인사로 판별: {}", userRequest);
                return handleFarewellOnly(userRequest);
            }

            // ✅ 3순위: 가격 추천 요청
            if (isPriceRecommendationRequest(userRequest)) {
                log.info("가격 추천으로 판별: {}", userRequest);
                return forwardToPriceRecommendation(userRequest);
            }

            // ✅ 4순위: 상품 검색/추천 요청
            log.info("상품 추천으로 처리: {}", userRequest);
            return forwardToProductRecommendation(userRequest);

        } catch (Exception e) {
            log.error("요청 처리 실패: {}", e.getMessage());
            return createErrorResponse("죄송해요, 요청을 처리하는 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 정확한 인사 메시지 판별 (엄격한 매칭)
     */
    private boolean isExactGreetingMessage(String message) {
        if (message == null) return false;

        String trimmed = message.trim().toLowerCase();

        // 정확히 인사말만 있는 경우
        String[] exactGreetings = {
                "안녕", "안녕하세요", "hello", "hi", "하이", "헬로", "반갑습니다", "처음 뵙겠습니다"
        };

        for (String greeting : exactGreetings) {
            if (trimmed.equals(greeting)) {
                return true;
            }
        }

        // 인사말 + 간단한 구두점만 있는 경우
        for (String greeting : exactGreetings) {
            if (trimmed.equals(greeting + "!") ||
                    trimmed.equals(greeting + ".") ||
                    trimmed.equals(greeting + "~")) {
                return true;
            }
        }

        return false;
    }

    /**
     * ✅ 정확한 작별 인사 판별 (엄격한 매칭)
     */
    private boolean isExactFarewellMessage(String message) {
        if (message == null) return false;

        String trimmed = message.trim().toLowerCase();

        String[] exactFarewells = {
                "잘자", "잘가", "잘 가", "잘 있어", "bye", "goodbye", "안녕히", "수고"
        };

        for (String farewell : exactFarewells) {
            if (trimmed.equals(farewell) ||
                    trimmed.equals(farewell + "!") ||
                    trimmed.equals(farewell + ".") ||
                    trimmed.equals(farewell + "~")) {
                return true;
            }
        }

        return false;
    }

    /**
     * ✅ 인사 전용 응답 (상품 추천 절대 없음)
     */
    private ResponseEntity<Map<String, Object>> handleGreetingOnly(String userRequest) {
        String greetingResponse = "안녕하세요! 😊 중고거래 플랫폼에 오신 것을 환영합니다.\n\n" +
                "저는 다음과 같은 역할을 할 수 있으며, 무엇을 도와드릴까요?\n\n" +
                "🔍 **상품 검색 및 추천**\n" +
                "- 원하시는 상품을 찾아드려요\n" +
                "- 카테고리별 상품 추천 (최신순)\n" +
                "- 최근 등록 상품 안내\n\n" +
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
        result.put("sortedBy", "latest"); // 사용자 선호사항 반영
        result.put("dataSource", "GREETING_HANDLER");
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        log.info("인사 전용 응답 완료");
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 작별 인사 전용 응답
     */
    private ResponseEntity<Map<String, Object>> handleFarewellOnly(String userRequest) {
        String farewellResponse = generateFarewellResponse(userRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("response", farewellResponse);
        result.put("type", "FAREWELL_ONLY");
        result.put("dataSource", "FAREWELL_HANDLER");
        result.put("timestamp", LocalDateTime.now());
        result.put("success", true);

        log.info("작별 인사 전용 응답 완료");
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 가격 추천으로 포워딩 (텍스트만, 상품 카드 없음)
     */
    private ResponseEntity<Map<String, Object>> forwardToPriceRecommendation(String userRequest) {
        try {
            String response = priceRecommendationService.getDirectPriceRecommendation(userRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("type", "PRICE_RECOMMENDATION");
            result.put("forwardedTo", "PriceRecommendationService");
            result.put("dataSource", "INTERNAL_DB_ONLY");
            result.put("sortedBy", "latest"); // 사용자 선호사항 반영
            result.put("timestamp", LocalDateTime.now());
            result.put("success", true);
            // ✅ 가격 추천에는 상품 카드 없음

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return createErrorResponse("가격 추천 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * ✅ 상품 추천으로 포워딩 (메인 상품 + AI 추천 3개)
     */
    private ResponseEntity<Map<String, Object>> forwardToProductRecommendation(String userRequest) {
        try {
            // 메인 상품 추천 텍스트
            String response = promptService.generateRecommendationResponse(userRequest);
            String userIntent = analyzeUserIntent(userRequest);

            // ✅ 1단계: 메인 상품 검색 (가장 관련성 높은 1개)
            ItemResponseDTO mainItem = getMainRecommendedItem(userRequest);

            // ✅ 2단계: AI 추천 상품 3개 검색 (메인 상품 제외)
            List<ItemResponseDTO> aiRecommendedItems = getAIRecommendedItemsExcludingMain(userRequest, mainItem, 3);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("type", "PRODUCT_RECOMMENDATION");
            result.put("userIntent", userIntent);
            result.put("forwardedTo", "PromptService");
            result.put("mainItem", mainItem);
            result.put("recommendedItems", aiRecommendedItems);
            result.put("dataSource", "INTERNAL_DB_ONLY");
            result.put("sortedBy", "latest"); // 사용자 선호사항 [5] 반영
            result.put("timestamp", LocalDateTime.now());
            result.put("success", true);

            log.info("상품 추천 완료 - 메인: {}, AI 추천: {}개 (중복 제거됨)",
                    mainItem != null ? mainItem.getTitle() : "없음", aiRecommendedItems.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return createErrorResponse("상품 추천 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    private List<ItemResponseDTO> getAIRecommendedItemsExcludingMain(String userRequest, ItemResponseDTO mainItem, int limit) {
        try {
            String keyword = extractMainKeyword(userRequest);
            String intent = analyzeUserIntent(userRequest);

            List<Item> items = new ArrayList<>();

            // 메인 상품 ID 추출 (중복 제거용)
            Long mainItemId = mainItem != null ? mainItem.getId() : null;

            log.info("AI 추천 검색 시작 - 제외할 메인 상품 ID: {}", mainItemId);

            // 키워드 기반 검색
            if (keyword != null && !keyword.isEmpty()) {
                List<Item> keywordItems = itemRepository.findByStatusAndTitleContainingOrderByRegDateDesc(
                        Item.Status.판매중, keyword);

                // ✅ 메인 상품 제외
                List<Item> filteredKeywordItems = keywordItems.stream()
                        .filter(item -> mainItemId == null || !item.getItemid().equals(mainItemId))
                        .collect(Collectors.toList());

                items.addAll(filteredKeywordItems);
                log.info("키워드 '{}' 검색 결과: {}개 (메인 제외 후: {}개)", keyword, keywordItems.size(), filteredKeywordItems.size());
            }

            // 부족한 경우 최신 상품으로 보완 (메인 상품 제외)
            if (items.size() < limit) {
                List<Item> latestItems = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);

                List<Item> finalItems = items;
                List<Item> filteredLatestItems = latestItems.stream()
                        .filter(item -> mainItemId == null || !item.getItemid().equals(mainItemId))
                        .filter(item -> !finalItems.contains(item)) // 이미 추가된 상품도 제외
                        .toList();

                items.addAll(filteredLatestItems);
                log.info("최신 상품으로 보완: {}개 추가", filteredLatestItems.size());
            }

            // ✅ 의도에 따른 정렬
            items = sortItemsByIntent(items, intent);

            // ✅ 최종 중복 제거 및 제한
            List<ItemResponseDTO> result = items.stream()
                    .distinct() // 중복 제거
                    .limit(limit)
                    .map(ItemResponseDTO::from)
                    .filter(dto -> dto != null)
                    .filter(dto -> mainItemId == null || !dto.getId().equals(mainItemId)) // 최종 안전장치
                    .collect(Collectors.toList());

            log.info("AI 추천 완료: {}개 (중복 제거 완료)", result.size());
            return result;

        } catch (Exception e) {
            log.error("AI 추천 상품 검색 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 메인 추천 상품 1개 검색
     */
    private ItemResponseDTO getMainRecommendedItem(String userRequest) {
        try {
            String keyword = extractMainKeyword(userRequest);
            String intent = analyzeUserIntent(userRequest);

            log.info("메인 상품 검색 - 키워드: {}, 의도: {}", keyword, intent);

            List<Item> items = new ArrayList<>();

            // 키워드 기반 검색
            if (keyword != null && !keyword.isEmpty()) {
                items = itemRepository.findByStatusAndTitleContainingOrderByRegDateDesc(
                        Item.Status.판매중, keyword);
                log.info("키워드 '{}' 검색 결과: {}개", keyword, items.size());
            }

            // 키워드 매칭 실패 시 전체 상품 조회
            if (items.isEmpty()) {
                items = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);
                log.info("전체 상품 조회 결과: {}개", items.size());
            }

            if (items.isEmpty()) {
                log.warn("검색 가능한 상품이 없습니다.");
                return null;
            }

            // ✅ 사용자 의도에 따른 상품 선택
            Item selectedItem = selectItemByIntent(items, intent);

            log.info("메인 상품 선택 완료 - ID: {}, 제목: {}", selectedItem.getItemid(), selectedItem.getTitle());
            return ItemResponseDTO.from(selectedItem);

        } catch (Exception e) {
            log.error("메인 추천 상품 검색 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ 의도에 따른 상품 선택
     */
    private Item selectItemByIntent(List<Item> items, String intent) {
        log.info("의도 '{}' 기반 상품 선택 시작 (총 {}개 상품)", intent, items.size());

        Item selected = switch (intent) {
            case "CHEAPEST" -> {
                log.info("최저가 상품 선택");
                yield items.stream()
                        .min((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                        .orElse(items.get(0));
            }
            case "MOST_EXPENSIVE" -> {
                log.info("최고가 상품 선택");
                yield items.stream()
                        .max((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                        .orElse(items.get(0));
            }
            case "LATEST" -> {
                log.info("최신 상품 선택 (사용자 선호사항 [5] 반영)");
                yield items.stream()
                        .max((a, b) -> a.getRegDate().compareTo(b.getRegDate()))
                        .orElse(items.get(0));
            }
            case "OLDEST" -> {
                log.info("가장 오래된 상품 선택");
                yield items.stream()
                        .min((a, b) -> a.getRegDate().compareTo(b.getRegDate()))
                        .orElse(items.get(0));
            }
            case "POPULAR" -> {
                log.info("인기 상품 선택 (조회수 기준)");
                yield items.stream()
                        .max((a, b) -> Integer.compare(
                                a.getViewCount() != null ? a.getViewCount() : 0,
                                b.getViewCount() != null ? b.getViewCount() : 0))
                        .orElse(items.get(0));
            }
            default -> {
                log.info("기본값: 최신 상품 선택 (사용자 선호사항 [5] 반영)");
                yield items.stream()
                        .max((a, b) -> a.getRegDate().compareTo(b.getRegDate()))
                        .orElse(items.get(0));
            }
        };

        log.info("선택된 상품 - ID: {}, 제목: {}, 가격: {}원",
                selected.getItemid(), selected.getTitle(), selected.getPrice());

        return selected;
    }

    /**
     * ✅ AI 추천 상품 3개도 의도 반영 (선택사항)
     */
    private List<ItemResponseDTO> getAIRecommendedItems(String userRequest, int limit) {
        try {
            String keyword = extractMainKeyword(userRequest);
            String intent = analyzeUserIntent(userRequest);

            List<Item> items = new ArrayList<>();

            // 키워드 기반 검색
            if (keyword != null && !keyword.isEmpty()) {
                List<Item> keywordItems = itemRepository.findByStatusAndTitleContainingOrderByRegDateDesc(
                        Item.Status.판매중, keyword);
                items.addAll(keywordItems);
            }

            // 부족한 경우 최신 상품으로 보완
            if (items.size() < limit) {
                List<Item> latestItems = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);
                List<Item> finalItems = items;
                items.addAll(latestItems.stream()
                        .filter(item -> !finalItems.contains(item))
                        .toList());
            }

            // ✅ 의도에 따른 정렬
            items = sortItemsByIntent(items, intent);

            return items.stream()
                    .limit(limit)
                    .map(ItemResponseDTO::from)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("AI 추천 상품 검색 실패: {}", e.getMessage());
            return List.of();
        }
    }
    /**
     * ✅ 의도에 따른 상품 정렬
     */
    private List<Item> sortItemsByIntent(List<Item> items, String intent) {
        return switch (intent) {
            case "CHEAPEST" -> items.stream()
                    .sorted((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                    .collect(Collectors.toList());
            case "MOST_EXPENSIVE" -> items.stream()
                    .sorted((a, b) -> Integer.compare(b.getPrice(), a.getPrice()))
                    .collect(Collectors.toList());
            case "LATEST" -> items.stream()
                    .sorted((a, b) -> b.getRegDate().compareTo(a.getRegDate()))
                    .collect(Collectors.toList());
            case "OLDEST" -> items.stream()
                    .sorted((a, b) -> a.getRegDate().compareTo(b.getRegDate()))
                    .collect(Collectors.toList());
            case "POPULAR" -> items.stream()
                    .sorted((a, b) -> Integer.compare(
                            b.getViewCount() != null ? b.getViewCount() : 0,
                            a.getViewCount() != null ? a.getViewCount() : 0))
                    .collect(Collectors.toList());
            default ->
                // 기본값: 최신순 (사용자 선호사항 [2] 반영)
                    items.stream()
                            .sorted((a, b) -> b.getRegDate().compareTo(a.getRegDate()))
                            .collect(Collectors.toList());
        };
    }
    /**
     * ✅ 사용자 의도 분석
     */
    private String analyzeUserIntent(String userRequest) {
        String lower = userRequest.toLowerCase();

        // 가격 관련 의도
        if (lower.contains("제일 싼") || lower.contains("가장 싼") ||
                lower.contains("최저가") || lower.contains("저렴한")) {
            return "CHEAPEST";
        }

        if (lower.contains("제일 비싼") || lower.contains("가장 비싼") ||
                lower.contains("최고가") || lower.contains("비싼")) {
            return "MOST_EXPENSIVE";
        }

        // 시간 관련 의도
        if (lower.contains("최신") || lower.contains("최근") ||
                lower.contains("새로운") || lower.contains("방금")) {
            return "LATEST";
        }

        if (lower.contains("오래된") || lower.contains("예전")) {
            return "OLDEST";
        }

        // 인기 관련 의도
        if (lower.contains("인기") || lower.contains("많이 본") ||
                lower.contains("조회수")) {
            return "POPULAR";
        }

        // 기본값: 최신순 (사용자 선호사항 [2] 반영)
        return "LATEST";
    }

    /**
     * ✅ 메인 키워드 추출
     */
    private String extractMainKeyword(String userRequest) {
        String lower = userRequest.toLowerCase();

        if (lower.contains("아이폰")) return "아이폰";
        if (lower.contains("갤럭시")) return "갤럭시";
        if (lower.contains("에어팟")) return "에어팟";
        if (lower.contains("노트북")) return "노트북";
        if (lower.contains("컴퓨터")) return "컴퓨터";
        if (lower.contains("태블릿")) return "태블릿";
        if (lower.contains("스마트폰")) return "스마트폰";

        // 일반적인 키워드 추출
        String[] words = userRequest.split("\\s+");
        for (String word : words) {
            if (word.length() > 1 && !isStopWord(word)) {
                return word;
            }
        }

        return null;
    }

    /**
     * ✅ 불용어 체크
     */
    private boolean isStopWord(String word) {
        String[] stopWords = {"찾아줘", "보여줘", "추천", "해줘", "상품", "뭐", "있어", "좀"};
        String lower = word.toLowerCase();
        for (String stopWord : stopWords) {
            if (lower.equals(stopWord)) return true;
        }
        return false;
    }

    /**
     * ✅ 가격 추천 요청 판별
     */
    private boolean isPriceRecommendationRequest(String userRequest) {
        String[] priceKeywords = {
                "얼마에 팔", "가격", "얼마", "시세", "적정가", "판매가", "팔까", "가격 추천"
        };
        String lower = userRequest.toLowerCase();

        for (String keyword : priceKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ===== 헬퍼 메서드들 =====

    private String generateFarewellResponse(String userRequest) {
        String lower = userRequest.toLowerCase().trim();

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

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, String error) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("response", message);
        errorResult.put("error", error);
        errorResult.put("success", false);
        errorResult.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(500).body(errorResult);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRecommendationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "unified-recommendation");
        status.put("status", "active");
        status.put("greetingHandling", "STRICT_MATCHING");
        status.put("sortedBy", "latest"); // 사용자 선호사항 반영
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
}
