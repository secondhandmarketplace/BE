package com.kdt.backend.controller;

import com.kdt.backend.dto.RecommendationRequest;
import com.kdt.backend.dto.RecommendationResponse;
import com.kdt.backend.service.PerplexityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final PerplexityService perplexityService;

    public RecommendationController(PerplexityService perplexityService) {
        this.perplexityService = perplexityService;
    }

    @PostMapping
    public Mono<RecommendationResponse> getRecommendations(@RequestBody RecommendationRequest request) {
        return perplexityService.getRecommendations(request.getUserRequest());
    }
}
