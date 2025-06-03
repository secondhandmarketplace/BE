package com.kdt.backend.controller;

import com.kdt.backend.dto.ReportRequestDTO;
import com.kdt.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> submitReport(@RequestBody ReportRequestDTO dto) {
        reportService.submitReport(dto);
        return ResponseEntity.ok("신고 완료");
    }
}
