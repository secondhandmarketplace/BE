package com.kdt.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDTO {
    private String userid;
    private String name;
    private String email;
    private double averageRating;
    private int reviewCount;
}
