package com.kdt.backend.controller;

import com.kdt.backend.dto.ChatMessageDTO;
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
@RequestMapping("/api/chat-messages")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * ✅ 채팅방 메시지 조회 (최근 등록순 정렬)
     */
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByChatRoom(@PathVariable Long chatRoomId) {
        try {
            log.info("채팅 메시지 조회 요청: chatRoomId={}", chatRoomId);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoom(chatRoomId);
            log.info("채팅 메시지 조회 완료: {}개", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("채팅 메시지 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * ✅ 메시지 전송 (실시간 메시징 지원)
     */
    @PostMapping("/room/{chatRoomId}")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageDTO messageDTO) {
        try {
            log.info("메시지 전송 요청: chatRoomId={}, senderId={}", chatRoomId, messageDTO.getSenderId());
            ChatMessageDTO sentMessage = chatMessageService.sendMessage(messageDTO, messageDTO.getSenderId());
            return ResponseEntity.ok(sentMessage);
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatMessageDTO.builder()
                            .content("메시지 전송에 실패했습니다.")
                            .build());
        }
    }

    /**
     * ✅ 메시지 읽음 처리
     */
    @PostMapping("/room/{chatRoomId}/read")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @RequestParam String userId) {
        try {
            log.info("메시지 읽음 처리: chatRoomId={}, userId={}", chatRoomId, userId);
            chatMessageService.markMessagesAsRead(chatRoomId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "읽음 처리가 완료되었습니다.");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "읽음 처리 중 오류가 발생했습니다.");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * ✅ 페이징 메시지 조회
     */
    @GetMapping("/room/{chatRoomId}/paged")
    public ResponseEntity<Map<String, Object>> getMessagesPaged(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.info("페이징 메시지 조회: chatRoomId={}, page={}, size={}", chatRoomId, page, size);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoom(chatRoomId);
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", messages.size());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("페이징 메시지 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ 메시지 검색 (클라이언트 필터링)
     */
    @GetMapping("/room/{chatRoomId}/search")
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
            @PathVariable Long chatRoomId,
            @RequestParam String keyword) {
        try {
            log.info("메시지 검색: chatRoomId={}, keyword={}", chatRoomId, keyword);
            List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoom(chatRoomId);
            List<ChatMessageDTO> filteredMessages = messages.stream()
                    .filter(msg -> msg.getContent().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            return ResponseEntity.ok(filteredMessages);
        } catch (Exception e) {
            log.error("메시지 검색 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * ✅ 서비스 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "chat-message-controller");
        status.put("status", "active");
        status.put("realTimeMessaging", true);
        status.put("springReactor", true);
        status.put("timestamp", LocalDateTime.now());
        status.put("features", new String[]{
                "실시간 메시지 전송", "메시지 읽음 처리", "페이징 조회", "메시지 검색"
        });
        return ResponseEntity.ok(status);
    }
}
