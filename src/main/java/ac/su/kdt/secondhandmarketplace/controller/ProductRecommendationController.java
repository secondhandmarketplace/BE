package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.service.PerplexityService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/recommendations")
public class ProductRecommendationController {

    private final PerplexityService perplexityService;

    public ProductRecommendationController(PerplexityService perplexityService) {
        this.perplexityService = perplexityService;
    }

    @PostMapping("/products")
    public Mono<String> getProductRecommendations(@RequestBody String userPreferences) {
        String prompt = "다음 사용자 선호도를 바탕으로 중고마켓에서 추천할 상품을 알려줘: " + userPreferences;
        return perplexityService.generateResponse(prompt);
    }
} 