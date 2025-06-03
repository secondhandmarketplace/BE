package com.kdt.backend.service;

import com.kdt.backend.dto.RecommendationResponse;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.ReviewRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemRecommendationService {

    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PerplexityService perplexityService;

    public Mono<RecommendationResponse> getRecommendations(String userRequest) {
        return perplexityService.getRecommendations(userRequest);
    }
} 