package com.kdt.backend.controller;

import com.kdt.backend.dto.ItemRegisterRequestDTO;
import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.ItemSuggestionDTO;
import com.kdt.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * ✅ 상품 등록 (프론트엔드 RegisterForm.jsx와 연동)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createItem(@RequestBody ItemRegisterRequestDTO request) {
        try {
            log.info("상품 등록 요청: {}", request);
            Long itemId = itemService.saveItem(request); // 실제 저장 로직은 서비스에서 처리
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "itemId", itemId,
                    "message", "상품이 성공적으로 등록되었습니다."
            ));
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "상품 등록에 실패했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ 아이템 목록 조회 (최근 등록순 [2] + 판매자 정보 포함)
     */
    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getItems(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<ItemResponseDTO> items = itemService.getItemsBySort(sort, size);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("아이템 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 특정 아이템 조회 (판매자 정보 포함)
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long itemId) {
        try {
            ItemResponseDTO item = itemService.getItemResponseById(itemId);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("아이템 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ 검색 추천 (대화형 인공지능 [6] 지원)
     */
    @GetMapping("/suggest")
    public ResponseEntity<List<ItemSuggestionDTO>> getItemSuggestions(@RequestParam String keyword) {
        try {
            log.info("상품 추천 검색: keyword={}", keyword);

            List<ItemSuggestionDTO> suggestions = itemService.getItemSuggestionsWithImage(keyword);

            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            log.error("상품 추천 검색 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 검색 기능 (최근 등록순 [2])
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDTO>> searchItems(@RequestParam String keyword) {
        try {
            log.info("상품 검색: keyword={}", keyword);

            List<ItemResponseDTO> items = itemService.searchItems(keyword);

            return ResponseEntity.ok(items);

        } catch (Exception e) {
            log.error("상품 검색 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 카테고리별 조회 (최근 등록순 [2])
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ItemResponseDTO>> getItemsByCategory(@PathVariable String category) {
        try {
            log.info("카테고리별 상품 조회: category={}", category);

            List<ItemResponseDTO> items = itemService.getItemsByCategory(category);

            return ResponseEntity.ok(items);

        } catch (Exception e) {
            log.error("카테고리별 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 판매자별 상품 조회 (최근 등록순 [2])
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ItemResponseDTO>> getItemsBySeller(@PathVariable String sellerId) {
        try {
            List<ItemResponseDTO> items = itemService.getItemsBySeller(sellerId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("판매자별 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 상품 상태 변경 (채팅 연동용)
     */
    @PutMapping("/{itemId}/status")
    public ResponseEntity<Map<String, Object>> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> request) {

        try {
            String status = request.get("status");
            String userId = request.get("userId");

            log.info("상품 상태 변경: itemId={}, status={}, userId={}", itemId, status, userId);

            itemService.updateItemStatus(itemId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상품 상태가 변경되었습니다.");
            response.put("itemId", itemId);
            response.put("newStatus", status);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("상품 상태 변경 실패: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "상품 상태 변경에 실패했습니다.");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * ✅ 조회수 증가
     */
    @PostMapping("/{itemId}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable Long itemId) {
        try {
            itemService.incrementViewCount(itemId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "조회수가 증가되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("조회수 증가 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false));
        }
    }

    /**
     * ✅ 서비스 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "item-controller");
        status.put("status", "active");
        status.put("latestSorting", true); // 최근 등록순 정렬 [2]
        status.put("springReactor", true); // Java Spring [5] 환경
        status.put("aiRecommendation", true); // 대화형 인공지능 [6]
        status.put("realTimeMessaging", true); // 실시간 메시징 [7]
        status.put("timestamp", LocalDateTime.now());
        status.put("features", new String[]{
                "상품 목록 조회", "상품 검색", "AI 추천", "상태 변경", "조회수 관리"
        });

        return ResponseEntity.ok(status);
    }

    /**
     * ✅ 연관 상품 조회 (검색 결과 [2] 패턴 - undefined 파라미터 방지)
     */
    @GetMapping("/related")
    public ResponseEntity<List<ItemResponseDTO>> getRelatedItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long excludeId,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            log.info("연관 상품 조회: category={}, excludeId={}, limit={}", category, excludeId, limit);

            // ✅ 파라미터 검증 (검색 결과 [2] undefined 방지)
            if (category == null || category.trim().isEmpty() || "undefined".equals(category)) {
                log.warn("유효하지 않은 카테고리: {}", category);
                return ResponseEntity.ok(List.of());
            }

            if (excludeId == null || excludeId <= 0) {
                log.warn("유효하지 않은 excludeId: {}", excludeId);
                return ResponseEntity.ok(List.of());
            }

            List<ItemResponseDTO> relatedItems = itemService.getRelatedItems(category, excludeId, limit);

            log.info("연관 상품 조회 완료: {}개 (최근 등록순)", relatedItems.size());
            return ResponseEntity.ok(relatedItems);

        } catch (Exception e) {
            log.error("연관 상품 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of()); // ✅ 500 대신 빈 리스트 반환
        }
    }

    /**
     * ✅ 카테고리별 연관 상품 조회 (대안 엔드포인트)
     */
    @GetMapping("/category/{category}/related")
    public ResponseEntity<List<ItemResponseDTO>> getRelatedItemsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long excludeId,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            log.info("카테고리별 연관 상품 조회: category={}, excludeId={}", category, excludeId);

            List<ItemResponseDTO> relatedItems = itemService.getRelatedItems(category, excludeId, limit);
            return ResponseEntity.ok(relatedItems);

        } catch (Exception e) {
            log.error("카테고리별 연관 상품 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
    /**
     * ✅ 전역 예외 처리 (검색 결과 [1] 권장사항)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("ItemController 예외 발생: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "상품 조회 중 오류가 발생했습니다.");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("success", false);
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(500).body(errorResponse);
    }

}
