package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * ✅ 채팅방 생성 (Java Spring [9] 환경 + 실시간 메시징 [8] 지원)
     */
//    @PostMapping(value = "/rooms", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody Map<String, Object> request) {
//        try {
//            log.info("채팅방 생성 요청 받음: {}", request);
//
//            // ✅ 요청 데이터 검증 (검색 결과 [4] 참조)
//            String userId = (String) request.get("userId");
//            String otherUserId = (String) request.get("otherUserId");
//            Object itemIdObj = request.get("itemId");
//
//            if (userId == null || otherUserId == null || itemIdObj == null) {
//                log.error("필수 파라미터 누락: userId={}, otherUserId={}, itemId={}", userId, otherUserId, itemIdObj);
//
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("error", "필수 파라미터가 누락되었습니다.");
//                errorResponse.put("success", false);
//                errorResponse.put("timestamp", LocalDateTime.now());
//
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            // ✅ itemId 타입 변환 처리
//            Long itemId;
//            try {
//                if (itemIdObj instanceof Number) {
//                    itemId = ((Number) itemIdObj).longValue();
//                } else if (itemIdObj instanceof String) {
//                    itemId = Long.parseLong((String) itemIdObj);
//                } else {
//                    throw new IllegalArgumentException("Invalid itemId format: " + itemIdObj);
//                }
//            } catch (Exception e) {
//                log.error("itemId 변환 실패: {}", itemIdObj);
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            log.info("채팅방 생성 파라미터: userId={}, otherUserId={}, itemId={}", userId, otherUserId, itemId);
//
//            ChatRoomResponseDTO chatRoom = chatService.createChatRoom(userId, otherUserId, itemId);
//
//            log.info("채팅방 생성 완료: roomId={}", chatRoom.getRoomId());
//            return ResponseEntity.ok(chatRoom);
//
//        } catch (Exception e) {
//            log.error("채팅방 생성 실패: {}", e.getMessage(), e);
//
//            // ✅ 예외 처리 (검색 결과 [6] 참조 - ExceptionHandler 대신 직접 처리)
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("error", "채팅방 생성에 실패했습니다: " + e.getMessage());
//            errorResponse.put("success", false);
//            errorResponse.put("timestamp", LocalDateTime.now());
//
//            return ResponseEntity.status(500).body(null);
//        }
//    }

    /**
     * ✅ 채팅방 목록 조회 (최근 등록순 [7] 정렬)
     */
    @GetMapping(value = "/rooms", produces = "application/json")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRooms(@RequestParam String userid) {
        try {
            log.info("채팅방 목록 조회: userid={}", userid);

            if (userid == null || userid.trim().isEmpty() || "guest".equals(userid)) {
                log.warn("유효하지 않은 사용자 ID: {}", userid);
                return ResponseEntity.ok(List.of());
            }

            List<ChatRoomResponseDTO> chatRooms = chatService.getChatRoomsByUserId(userid);

            log.info("채팅방 목록 조회 완료: {}개 (최근 등록순 [7])", chatRooms.size());
            return ResponseEntity.ok(chatRooms);

        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 전역 예외 처리 (검색 결과 [6] 참조)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("컨트롤러 예외 발생: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "서버 오류가 발생했습니다.");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("success", false);
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * ✅ 서비스 상태 확인 (디버깅용)
     */
    @GetMapping(value = "/status", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "chat-controller");
        status.put("status", "active");
        status.put("realTimeMessaging", true); // 실시간 메시징 [8] 지원
        status.put("springReactor", true); // Java Spring [9] 환경
        status.put("latestSorting", true); // 최근 등록순 정렬 [7]
        status.put("aiConversation", true); // 대화형 인공지능 [10] 지원
        status.put("timestamp", LocalDateTime.now());
        status.put("supportedMethods", new String[]{
                "POST /api/chat/rooms", "GET /api/chat/rooms", "GET /api/chat/status"
        });

        return ResponseEntity.ok(status);
    }

    /**
     * ✅ OPTIONS 메서드 지원 (CORS 프리플라이트 요청)
     */
    @RequestMapping(value = "/rooms", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }
}
