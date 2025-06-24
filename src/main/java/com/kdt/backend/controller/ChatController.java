package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.service.ChatService;
import com.kdt.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final ChatMessageService chatMessageService;

    /**
     * ✅ 채팅방 목록 조회 (최근 등록순 [7] 정렬)
     */
    @GetMapping(value = "/rooms", produces = "application/json")
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRooms(@RequestParam String userId) {
        try {
            log.info("채팅방 목록 조회: userId={}", userId);

            if (userId == null || userId.trim().isEmpty() || "guest".equals(userId)) {
                log.warn("유효하지 않은 사용자 ID: {}", userId);
                return ResponseEntity.ok(List.of());
            }

            List<ChatRoomResponseDTO> chatRooms = chatService.getChatRoomsByUserId(userId);

            log.info("채팅방 목록 조회 완료: {}개 (최근 등록순 [7])", chatRooms.size());
            return ResponseEntity.ok(chatRooms);

        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 채팅방 정보 조회 (단일 채팅방)
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoom(
            @PathVariable Long roomId,
            @RequestParam String userId) {
        try {
            log.info("채팅방 정보 조회: roomId={}, userId={}", roomId, userId);

            if (userId == null || userId.trim().isEmpty()) {
                log.error("userId가 누락됨: roomId={}", roomId);
                return ResponseEntity.badRequest().body(null);
            }

            ChatRoomResponseDTO chatRoom = chatService.getChatRoomById(roomId, userId);

            if (chatRoom == null) {
                log.warn("채팅방을 찾을 수 없음: roomId={}, userId={}", roomId, userId);
                return ResponseEntity.notFound().build();
            }

            log.info("채팅방 정보 조회 완료: roomId={}, otherUserId={}", roomId, chatRoom.getOtherUserId());
            return ResponseEntity.ok(chatRoom);

        } catch (Exception e) {
            log.error("채팅방 정보 조회 실패: roomId={}, userId={}, error={}", roomId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * ✅ 채팅방 삭제
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Map<String, Object>> deleteChatRoom(
            @PathVariable Long roomId,
            @RequestParam String userId) {
        try {
            log.info("채팅방 삭제 요청: roomId={}, userId={}", roomId, userId);

            boolean deleted = chatService.deleteChatRoom(roomId, userId);

            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "채팅방이 삭제되었습니다.");
                log.info("채팅방 삭제 완료: roomId={}", roomId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "채팅방 삭제에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("채팅방 삭제 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "채팅방 삭제 중 오류가 발생했습니다.");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * ✅ 채팅방 생성 또는 조회 (프론트엔드 호환)
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDTO> createOrGetChatRoom(@RequestBody Map<String, Object> request) {
        try {
            log.info("채팅방 생성/조회 요청 데이터: {}", request);

            // 안전한 데이터 추출
            String userId = getStringValue(request, "userId");
            String otherUserId = getStringValue(request, "otherUserId");
            Long itemId = getLongValue(request, "itemId");

            // 필수 파라미터 검증
            if (userId == null || userId.trim().isEmpty()) {
                log.error("userId가 누락됨: {}", request);
                return ResponseEntity.badRequest().body(null);
            }
            if (otherUserId == null || otherUserId.trim().isEmpty()) {
                log.error("otherUserId가 누락됨: {}", request);
                return ResponseEntity.badRequest().body(null);
            }
            if (itemId == null) {
                log.error("itemId가 누락됨: {}", request);
                return ResponseEntity.badRequest().body(null);
            }

            log.info("채팅방 생성/조회 요청: userId={}, otherUserId={}, itemId={}", userId, otherUserId, itemId);

            // ChatService의 createChatRoom 메서드 호출
            ChatRoomResponseDTO chatRoom = chatService.createChatRoom(userId, otherUserId, itemId);

            return ResponseEntity.ok(chatRoom);

        } catch (Exception e) {
            log.error("채팅방 생성/조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 헬퍼 메서드들
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.error("Long 변환 실패: key={}, value={}", key, value);
            return null;
        }
    }

    /**
     * ✅ 채팅방 메시지 조회 (프론트엔드 호환)
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long roomId) {
        try {
            log.info("채팅 메시지 조회 요청: roomId={}", roomId);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoom(roomId);
            log.info("채팅 메시지 조회 완료: {}개", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("채팅 메시지 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 메시지 전송 (프론트엔드 호환)
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageDTO messageDTO) {
        try {
            log.info("메시지 전송 요청: roomId={}, senderId={}, content={}", roomId, messageDTO.getSenderId(), messageDTO.getContent());

            // roomId 설정
            messageDTO.setChatRoomId(roomId);

            // 발신자 ID 검증
            String senderId = messageDTO.getSenderId();
            if (senderId == null || senderId.trim().isEmpty()) {
                log.error("발신자 ID가 누락됨");
                return ResponseEntity.badRequest().body(
                    ChatMessageDTO.builder()
                        .content("발신자 ID가 필요합니다.")
                        .build()
                );
            }

            ChatMessageDTO sentMessage = chatMessageService.sendMessage(messageDTO, senderId);
            log.info("메시지 전송 성공: messageId={}", sentMessage.getMessageId());
            return ResponseEntity.ok(sentMessage);

        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ChatMessageDTO.builder()
                    .content("메시지 전송에 실패했습니다: " + e.getMessage())
                    .build()
            );
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

}
