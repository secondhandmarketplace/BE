package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.service.PerplexityService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/perplexity")
public class PerplexityController {

    private final PerplexityService perplexityService;

    public PerplexityController(PerplexityService perplexityService) {
        this.perplexityService = perplexityService;
    }

    @PostMapping("/ask")
    public Mono<String> askQuestion(@RequestBody String prompt) {
        return perplexityService.generateResponse(prompt);
    }
} 