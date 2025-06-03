package com.kdt.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDTO {
    private String reporterId;
    private String reportedId;
    private Long itemId; // nullable
    private String reason;
}
