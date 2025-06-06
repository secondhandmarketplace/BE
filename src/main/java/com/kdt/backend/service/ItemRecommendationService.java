package com.kdt.backend.service;

import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.RecommendationResponse;
import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.ReviewRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRecommendationService {

    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PerplexityService perplexityService;
    private final PromptService promptService;

    /**
     * 일반 상품 추천 (AI 기반) - 최근 등록순 우선 적용
     */
    public Mono<RecommendationResponse> getRecommendations(String userRequest) {
        log.info("일반 상품 추천 요청: {}", userRequest);

        return Mono.fromCallable(() -> analyzeUserRequest(userRequest))
                .flatMap(this::generateRecommendationResponse)
                .doOnNext(response -> {
                    if (response.getRecommendedItems() != null) {
                        log.info("추천 완료: {} 개 상품", response.getRecommendedItems().size());
                    } else {
                        log.info("추천 완료: 0 개 상품");
                    }
                })
                .doOnError(error -> log.error("추천 실패: {}", error.getMessage()));
    }

    /**
     * 실시간 상품 추천 (DB 기반, AI 없이)
     */
    public String getInstantRecommendation(String userRequest) {
        try {
            return promptService.generateRecommendationResponse(userRequest);
        } catch (Exception e) {
            log.error("실시간 추천 실패: {}", e.getMessage());
            return "죄송해요, 추천 중 오류가 발생했습니다.";
        }
    }

    /**
     * 카테고리별 추천 (최근 등록순)
     */
    public Mono<RecommendationResponse> getCategoryRecommendations(String category, int limit) {
        return Mono.fromCallable(() -> {
            try {
                List<Item> items = itemRepository.findByCategoryAndStatusOrderByRegDateDesc(
                        category, Item.Status.판매중);

                return buildRecommendationResponse(
                        items.stream().limit(limit).collect(Collectors.toList()),
                        category + " 카테고리의 최신 상품들입니다!"
                );
            } catch (Exception e) {
                log.error("카테고리별 추천 실패: {}", e.getMessage());
                return buildEmptyRecommendationResponse("카테고리별 추천 중 오류가 발생했습니다.");
            }
        });
    }

    /**
     * 인기 상품 추천
     */
    public Mono<RecommendationResponse> getPopularRecommendations(int limit) {
        return Mono.fromCallable(() -> {
            try {
                List<Item> items = itemRepository.findByStatusOrderByViewCountDesc(Item.Status.판매중);

                return buildRecommendationResponse(
                        items.stream().limit(limit).collect(Collectors.toList()),
                        "인기 상품들을 추천드립니다!"
                );
            } catch (Exception e) {
                log.error("인기 상품 추천 실패: {}", e.getMessage());
                return buildEmptyRecommendationResponse("인기 상품 추천 중 오류가 발생했습니다.");
            }
        });
    }

    /**
     * 최신 상품 추천 (최근 등록순)
     */
    public Mono<RecommendationResponse> getLatestRecommendations(int limit) {
        return Mono.fromCallable(() -> {
            try {
                List<Item> items = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);

                return buildRecommendationResponse(
                        items.stream().limit(limit).collect(Collectors.toList()),
                        "최근에 등록된 상품들입니다!"
                );
            } catch (Exception e) {
                log.error("최신 상품 추천 실패: {}", e.getMessage());
                return buildEmptyRecommendationResponse("최신 상품 추천 중 오류가 발생했습니다.");
            }
        });
    }

    /**
     * 가격대별 추천 (최근 등록순 우선)
     */
    public Mono<RecommendationResponse> getPriceRangeRecommendations(int minPrice, int maxPrice, int limit) {
        return Mono.fromCallable(() -> {
            try {
                List<Item> items = itemRepository.findByStatusAndPriceBetweenOrderByRegDateDesc(
                        Item.Status.판매중, minPrice, maxPrice);

                return buildRecommendationResponse(
                        items.stream().limit(limit).collect(Collectors.toList()),
                        String.format("%,d원 ~ %,d원 가격대의 최신 상품들입니다!", minPrice, maxPrice)
                );
            } catch (Exception e) {
                log.error("가격대별 추천 실패: {}", e.getMessage());
                return buildEmptyRecommendationResponse("가격대별 추천 중 오류가 발생했습니다.");
            }
        });
    }

    /**
     * 사용자 요청 분석
     */
    private RecommendationContext analyzeUserRequest(String userRequest) {
        RecommendationContext context = new RecommendationContext();
        context.setUserRequest(userRequest);
        context.setRequestTime(LocalDateTime.now());

        String lowerRequest = userRequest.toLowerCase();

        if (lowerRequest.contains("인기")) {
            context.setRequestType("POPULAR");
        } else if (lowerRequest.contains("최신") || lowerRequest.contains("최근")) {
            context.setRequestType("LATEST");
        } else if (lowerRequest.contains("저렴") || lowerRequest.contains("싼")) {
            context.setRequestType("CHEAP");
        } else {
            context.setRequestType("GENERAL");
        }

        return context;
    }

    /**
     * 추천 응답 생성 (최근 등록순 우선)
     */
    private Mono<RecommendationResponse> generateRecommendationResponse(RecommendationContext context) {
        return Mono.fromCallable(() -> {
            try {
                List<Item> items;
                String explanation;

                switch (context.getRequestType()) {
                    case "POPULAR":
                        items = itemRepository.findByStatusOrderByViewCountDesc(Item.Status.판매중);
                        explanation = "인기 상품들을 추천드립니다!";
                        break;
                    case "LATEST":
                        items = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);
                        explanation = "최근에 등록된 상품들입니다!";
                        break;
                    case "CHEAP":
                        items = itemRepository.findByStatusAndPriceGreaterThanOrderByPriceAsc(Item.Status.판매중, 0);
                        explanation = "저렴한 상품들을 추천드립니다!";
                        break;
                    default:
                        items = itemRepository.findByStatusOrderByRegDateDesc(Item.Status.판매중);
                        explanation = "최신 추천 상품들입니다!";
                        break;
                }

                return buildRecommendationResponse(
                        items.stream().limit(10).collect(Collectors.toList()),
                        explanation
                );
            } catch (Exception e) {
                log.error("추천 응답 생성 실패: {}", e.getMessage());
                return buildEmptyRecommendationResponse("추천 생성 중 오류가 발생했습니다.");
            }
        });
    }

    /**
     * 추천 응답 객체 생성
     */
    private RecommendationResponse buildRecommendationResponse(List<Item> items, String explanation) {
        RecommendationResponse response = new RecommendationResponse();

        if (items != null && !items.isEmpty()) {
            List<ItemResponseDTO> itemDTOs = items.stream()
                    .map(this::convertToItemResponseDTO)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            response.setRecommendedItems(itemDTOs);
            response.setTotalCount(itemDTOs.size());
        } else {
            response.setRecommendedItems(new ArrayList<>());
            response.setTotalCount(0);
        }

        response.setExplanation(explanation);
        response.setTimestamp(LocalDateTime.now());
        response.setSortedBy("latest"); // 최근 등록순
        response.setSuccess(true);

        return response;
    }

    /**
     * 빈 추천 응답 생성
     */
    private RecommendationResponse buildEmptyRecommendationResponse(String explanation) {
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendedItems(new ArrayList<>());
        response.setExplanation(explanation);
        response.setTotalCount(0);
        response.setTimestamp(LocalDateTime.now());
        response.setSortedBy("latest");
        response.setSuccess(false);
        return response;
    }

    /**
     * Item을 ItemResponseDTO로 변환
     */
    private ItemResponseDTO convertToItemResponseDTO(Item item) {
        try {
            if (item == null) {
                return null;
            }
            return ItemResponseDTO.from(item);
        } catch (Exception e) {
            log.error("ItemResponseDTO 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 추천 컨텍스트 내부 클래스
     */
    public static class RecommendationContext {
        private String userRequest;
        private LocalDateTime requestTime;
        private String requestType;

        public String getUserRequest() { return userRequest; }
        public void setUserRequest(String userRequest) { this.userRequest = userRequest; }

        public LocalDateTime getRequestTime() { return requestTime; }
        public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
    }
}
