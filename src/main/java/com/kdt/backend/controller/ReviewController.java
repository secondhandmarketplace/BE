package com.kdt.backend.controller;

import com.kdt.backend.dto.ReviewRequestDTO;
import com.kdt.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kdt.backend.dto.ReviewResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDTO dto) {
        reviewService.createReview(dto);
        return ResponseEntity.ok("리뷰가 저장되었습니다.");
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsBySeller(@PathVariable String sellerId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsForSeller(sellerId);
        return ResponseEntity.ok(reviews);
    }
}
