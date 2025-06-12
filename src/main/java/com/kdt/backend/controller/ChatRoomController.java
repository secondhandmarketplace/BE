package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatRoomRequestDTO;
import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/rooms")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * ✅ 채팅방 생성 또는 조회
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponseDTO> createOrGetChatRoom(@RequestBody ChatRoomRequestDTO request) {
        try {
            log.info("채팅방 생성/조회 요청: {}", request);
            ChatRoomResponseDTO response = chatRoomService.createOrGetChatRoom(
                    request.getBuyerId(),
                    request.getSellerId(),
                    request.getItemTransactionId()
            );
            log.info("채팅방 생성/조회 완료: roomId={}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채팅방 생성/조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * ✅ 채팅방 목록 조회 (최근 등록순 정렬)
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRoomsByUser(@RequestParam String userId) {
        try {
            log.info("채팅방 목록 조회: userId={}", userId);
            List<ChatRoomResponseDTO> rooms = chatRoomService.getChatRoomsByUser(userId);
            log.info("채팅방 목록 조회 완료: {}개", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ✅ 채팅방 상세 조회
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomDetails(@PathVariable Long chatRoomId, @RequestParam String userId) {
        try {
            log.info("채팅방 상세 조회: chatRoomId={}, userId={}", chatRoomId, userId);
            ChatRoomResponseDTO roomDetails = chatRoomService.getChatRoomDetails(chatRoomId, userId);
            return ResponseEntity.ok(roomDetails);
        } catch (Exception e) {
            log.error("채팅방 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * ✅ 전역 예외 처리
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
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "chat-room-controller");
        status.put("status", "active");
        status.put("realTimeMessaging", true);
        status.put("springReactor", true);
        status.put("latestSorting", true);
        status.put("aiConversation", true);
        status.put("timestamp", LocalDateTime.now());
        status.put("supportedMethods", new String[]{
                "POST /api/chat/rooms", "GET /api/chat/rooms", "GET /api/chat/rooms/{id}", "GET /api/chat/rooms/status"
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
