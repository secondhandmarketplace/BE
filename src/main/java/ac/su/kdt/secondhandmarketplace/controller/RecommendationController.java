package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.dto.RecommendationRequest;
import ac.su.kdt.secondhandmarketplace.dto.RecommendationResponse;
import ac.su.kdt.secondhandmarketplace.service.PerplexityService;
import org.springframework.web.bind.annotation.*;
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
        return perplexityService.getRecommendations(request);
    }
} 