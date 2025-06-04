package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.ProductRecommendationResponse;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import ac.su.kdt.secondhandmarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRecommendationService {
    
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PerplexityService perplexityService;
    
    public Mono<List<ProductRecommendationResponse>> getRecommendations(String userRequest) {
        return perplexityService.getRecommendations(userRequest);
    }
} 