package com.kdt.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginResponseDto {
    private boolean success;
    private String userId;
    private String userName;
    private String message;
    private String token;
    private LocalDateTime timestamp;
}