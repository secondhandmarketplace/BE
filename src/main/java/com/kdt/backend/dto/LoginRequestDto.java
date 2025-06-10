package com.kdt.backend.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String userid;
    private String password;
}