package com.kdt.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ 채팅 관련 예외 처리기
 */
@RestControllerAdvice
@Slf4j
public class ChatExceptionHandler {

    /**
     * 채팅방을 찾을 수 없는 경우
     */
    @ExceptionHandler(ChatException.ChatRoomNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleChatRoomNotFound(ChatException.ChatRoomNotFoundException e) {
        log.warn("채팅방을 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", e.getMessage());
    }

    /**
     * 사용자를 찾을 수 없는 경우
     */
    @ExceptionHandler(ChatException.UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(ChatException.UserNotFoundException e) {
        log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", e.getMessage());
    }

    /**
     * 권한이 없는 경우
     */
    @ExceptionHandler(ChatException.UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(ChatException.UnauthorizedAccessException e) {
        log.warn("권한 없는 접근: {}", e.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS", e.getMessage());
    }

    /**
     * 메시지 전송 실패
     */
    @ExceptionHandler(ChatException.MessageSendFailedException.class)
    public ResponseEntity<Map<String, Object>> handleMessageSendFailed(ChatException.MessageSendFailedException e) {
        log.error("메시지 전송 실패: {}", e.getMessage(), e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "MESSAGE_SEND_FAILED", e.getMessage());
    }

    /**
     * 채팅방 생성 실패
     */
    @ExceptionHandler(ChatException.ChatRoomCreationException.class)
    public ResponseEntity<Map<String, Object>> handleChatRoomCreationFailed(ChatException.ChatRoomCreationException e) {
        log.error("채팅방 생성 실패: {}", e.getMessage(), e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_ROOM_CREATION_FAILED", e.getMessage());
    }

    /**
     * 일반적인 채팅 예외
     */
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<Map<String, Object>> handleChatException(ChatException e) {
        log.error("채팅 예외: {}", e.getMessage(), e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_ERROR", e.getMessage());
    }

    /**
     * 에러 응답 생성 헬퍼 메서드
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}
