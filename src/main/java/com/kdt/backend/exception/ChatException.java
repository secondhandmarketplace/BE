package com.kdt.backend.exception;

/**
 * ✅ 채팅 관련 커스텀 예외 클래스
 */
public class ChatException extends RuntimeException {
    
    public ChatException(String message) {
        super(message);
    }
    
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 채팅방을 찾을 수 없는 경우
     */
    public static class ChatRoomNotFoundException extends ChatException {
        public ChatRoomNotFoundException(Long roomId) {
            super("채팅방을 찾을 수 없습니다: " + roomId);
        }
    }
    
    /**
     * 사용자를 찾을 수 없는 경우
     */
    public static class UserNotFoundException extends ChatException {
        public UserNotFoundException(String userId) {
            super("사용자를 찾을 수 없습니다: " + userId);
        }
    }
    
    /**
     * 권한이 없는 경우
     */
    public static class UnauthorizedAccessException extends ChatException {
        public UnauthorizedAccessException(String action) {
            super("권한이 없습니다: " + action);
        }
    }
    
    /**
     * 메시지 전송 실패
     */
    public static class MessageSendFailedException extends ChatException {
        public MessageSendFailedException(String reason) {
            super("메시지 전송에 실패했습니다: " + reason);
        }
        
        public MessageSendFailedException(String reason, Throwable cause) {
            super("메시지 전송에 실패했습니다: " + reason, cause);
        }
    }
    
    /**
     * 채팅방 생성 실패
     */
    public static class ChatRoomCreationException extends ChatException {
        public ChatRoomCreationException(String reason) {
            super("채팅방 생성에 실패했습니다: " + reason);
        }
        
        public ChatRoomCreationException(String reason, Throwable cause) {
            super("채팅방 생성에 실패했습니다: " + reason, cause);
        }
    }
}
