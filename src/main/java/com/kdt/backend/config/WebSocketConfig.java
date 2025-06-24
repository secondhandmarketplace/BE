package com.kdt.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter; // ✅ Spring의 MessageConverter 임포트
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        return false; // 기본 컨버터 추가 비활성화
    }

    // 핸드셰이크 시 Principal 생성
    private static class CustomHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(
                ServerHttpRequest request,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes
        ) {
            // 세션 기반: HTTP 세션에서 사용자 정보 추출
            Principal principal = request.getPrincipal();
            if (principal != null) {
                return principal;
            }

            // 토큰 기반: JWT 등으로 사용자 식별
            String token = extractToken(request);
            if (token != null && isValidToken(token)) {
                return () -> extractUsername(token); // 사용자명 반환
            }

            // 익명 사용자 처리
            return () -> "anonymous-" + UUID.randomUUID();
        }

        private String extractToken(ServerHttpRequest request) {
            // Authorization 헤더에서 토큰 추출
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            // 쿼리 파라미터에서 토큰 추출 (fallback)
            String token = request.getURI().getQuery();
            if (token != null && token.contains("token=")) {
                return token.split("token=")[1].split("&")[0];
            }

            return null;
        }

        private boolean isValidToken(String token) {
            // TODO: JWT 토큰 검증 로직 구현
            // 현재는 기본적인 null 체크만 수행
            return token != null && !token.trim().isEmpty();
        }

        private String extractUsername(String token) {
            // TODO: JWT 토큰에서 사용자명 추출 로직 구현
            // 현재는 임시로 토큰의 일부를 반환
            return "user-" + token.hashCode();
        }
    }
}
