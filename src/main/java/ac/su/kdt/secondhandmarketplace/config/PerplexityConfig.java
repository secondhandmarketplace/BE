package ac.su.kdt.secondhandmarketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PerplexityConfig {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient perplexityWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
} 