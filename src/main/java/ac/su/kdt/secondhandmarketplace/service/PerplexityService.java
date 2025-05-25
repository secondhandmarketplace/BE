package ac.su.kdt.secondhandmarketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Service
public class PerplexityService {

    private final WebClient perplexityWebClient;

    public PerplexityService(WebClient perplexityWebClient) {
        this.perplexityWebClient = perplexityWebClient;
    }

    public Mono<String> generateResponse(String prompt) {
        return perplexityWebClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                    "model", "sonar-pro",
                    "messages", List.of(Map.of("role", "user", "content", prompt))
                ))
                .retrieve()
                .bodyToMono(String.class);
    }
} 