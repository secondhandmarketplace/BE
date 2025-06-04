package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.ProductRecommendationDTO;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.RecommendationCriteria;
import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
import ac.su.kdt.secondhandmarketplace.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.ProductRecommendationResponse;
import ac.su.kdt.secondhandmarketplace.config.PerplexityConfig;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.RecommendationRequest;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.RecommendationResponse;

@Service
@RequiredArgsConstructor
public class PerplexityService {

    private final WebClient perplexityWebClient;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final PromptService promptService;
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\[상품(\\d+)\\]([^\\[]+)");

    @Autowired
    public PerplexityService(PerplexityConfig perplexityConfig, ProductRepository productRepository, ReviewRepository reviewRepository, PromptService promptService) {
        this.perplexityWebClient = WebClient.builder()
                .baseUrl(perplexityConfig.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + perplexityConfig.getApiKey())
                .build();
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.promptService = promptService;
    }

    /**
     * 사용자의 자연어 요청을 받아 상품 추천을 수행하는 메인 메소드
     * 1. 사용자 요청 분석
     * 2. DB에서 데이터 수집
     * 3. LLM 프롬프트 생성
     * 4. LLM API 호출 및 응답 처리
     * @return 추천된 상품 ID와 추천 이유 목록
     */
    public Mono<List<ProductRecommendationResponse>> getRecommendations(String userRequest) {
        // 1. 사용자 요청 분석
        RecommendationCriteria criteria = analyzeUserRequest(userRequest);
        
        // 2. DB에서 데이터 수집
        List<Product> products = collectProductData(criteria);
        List<ProductRecommendationDTO> productDTOs = products.stream()
            .map(product -> ProductRecommendationDTO.fromEntity(product, reviewRepository))
            .collect(Collectors.toList());
        
        // 3. LLM 프롬프트 생성
        String prompt = promptService.generateRecommendationPrompt(criteria, productDTOs);
        
        // 4. LLM API 호출 및 응답 처리
        return callPerplexityAPI(prompt, productDTOs);
    }

    /**
     * 사용자의 자연어 요청을 분석하여 추천 기준을 추출하는 메소드
     * - 매너 점수, 평점, 상품명, 가격 범위, 위치, 정렬 기준 등을 추출
     * - 정규표현식을 사용하여 각 조건을 파싱
     */
    private RecommendationCriteria analyzeUserRequest(String userRequest) {
        RecommendationCriteria criteria = new RecommendationCriteria();
        
        // 기본값 설정
        criteria.setMinMannerScore(0.0);  // 기본값 0점
        criteria.setMinRating(0.0);       // 기본값 0점
        
        // 매너 점수 조건 추출
        Pattern mannerPattern = Pattern.compile("매너점수\\s*(\\d+)점\\s*이상");
        Matcher mannerMatcher = mannerPattern.matcher(userRequest);
        if (mannerMatcher.find()) {
            criteria.setMinMannerScore(Double.parseDouble(mannerMatcher.group(1)));
        }
        
        // 리뷰 평점 조건 추출
        Pattern ratingPattern = Pattern.compile("평점\\s*(\\d+)점\\s*이상");
        Matcher ratingMatcher = ratingPattern.matcher(userRequest);
        if (ratingMatcher.find()) {
            criteria.setMinRating(Double.parseDouble(ratingMatcher.group(1)));
        }
        
        // 상품명 추출
        Pattern productPattern = Pattern.compile("(AirPods Pro|아이폰|맥북|갤럭시|노트북|컴퓨터|모니터|키보드|마우스|헤드폰|이어폰|스마트폰|태블릿|카메라|게임기|의류|신발|가방|가구|전자제품|도서|음반|영화|스포츠용품|취미용품|기타)");
        Matcher productMatcher = productPattern.matcher(userRequest);
        if (productMatcher.find()) {
            criteria.setProductName(productMatcher.group(1));
        }
        
        // 가격 범위 추출
        Pattern pricePattern = Pattern.compile("(\\d+)만원\\s*이하");
        Matcher priceMatcher = pricePattern.matcher(userRequest);
        if (priceMatcher.find()) {
            criteria.setMaxPrice(new BigDecimal(priceMatcher.group(1)).multiply(new BigDecimal("10000")));
        }
        
        // 위치 정보 추출 (정확한 주소명)
        Pattern locationPattern = Pattern.compile("(서울시|부산시|대구시|인천시|광주시|대전시|울산시|세종시|경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주도)\\s*([가-힣]+(시|군|구))?\\s*([가-힣]+(읍|면|동))?");
        Matcher locationMatcher = locationPattern.matcher(userRequest);
        if (locationMatcher.find()) {
            StringBuilder address = new StringBuilder();
            for (int i = 1; i <= locationMatcher.groupCount(); i++) {
                if (locationMatcher.group(i) != null) {
                    address.append(locationMatcher.group(i)).append(" ");
                }
            }
            criteria.setLocation(address.toString().trim());
        }
        
        // 정렬 기준 설정
        if (userRequest.contains("가격순")) {
            criteria.setSortBy("price");
            criteria.setSortDirection(userRequest.contains("높은") ? "desc" : "asc");
        } else if (userRequest.contains("평점순")) {
            criteria.setSortBy("rating");
            criteria.setSortDirection("desc");
        } else if (userRequest.contains("조회순")) {
            criteria.setSortBy("viewCount");
            criteria.setSortDirection("desc");
        }
        
        return criteria;
    }

    /**
     * 추출된 추천 기준을 바탕으로 DB에서 상품 데이터를 조회하는 메소드
     * - 상품명, 카테고리, 가격 범위, 위치, 매너 점수 등을 기준으로 필터링
     * - 정렬 기준에 따라 결과를 정렬
     */
    private List<Product> collectProductData(RecommendationCriteria criteria) { // 상품 추천 기준에 맞는 상품을 조회하는 메서드
        return productRepository.findByRecommendationCriteria(
            criteria.getProductName(),
            criteria.getCategory(),
            criteria.getMaxPrice(),
            criteria.getMinPrice(),
            criteria.getLocation(),
            criteria.getMinMannerScore(),
            criteria.getSortBy(),
            criteria.getSortDirection()
        );
    }

    /**
     * 일반 채팅을 위한 Perplexity API 호출 메소드
     */
    public Mono<Map<String, Object>> chat(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "sonar-pro");
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);
        requestBody.put("top_p", 0.9);

        return perplexityWebClient.post()
                .uri("/chat/completions")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * 상품 추천을 위한 Perplexity API 호출 메소드
     */
    private Mono<List<ProductRecommendationResponse>> callPerplexityAPI(String prompt, List<ProductRecommendationDTO> allProducts) {
        return chat(prompt)
            .map(response -> parseLLMResponse(response, allProducts));
    }

    /**
     * LLM의 응답을 파싱하여 추천된 상품 ID와 이유 목록을 생성하는 메소드
     * - LLM 응답에서 상품 ID와 추천 이유를 추출
     * - 추출된 정보를 기반으로 ProductRecommendationResponse 객체 생성
     * - 예외 발생 시 RuntimeException으로 래핑
     */
    private List<ProductRecommendationResponse> parseLLMResponse(Map<String, Object> response, List<ProductRecommendationDTO> allProducts) {
        try {
            List<ProductRecommendationResponse> recommendations = new ArrayList<>();
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");
            
            Matcher matcher = PRODUCT_ID_PATTERN.matcher(content);
            while (matcher.find()) {
                Long productId = Long.parseLong(matcher.group(1));
                String reason = matcher.group(2).trim();
                
                allProducts.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .ifPresent(p -> recommendations.add(new ProductRecommendationResponse(productId, reason)));
            }
            
            return recommendations;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    public Mono<RecommendationResponse> getRecommendations(RecommendationRequest request) {
        // 사용자 요청 분석
        RecommendationCriteria criteria = analyzeUserRequest(request.getUserRequest());
        
        // 데이터베이스에서 상품 정보 조회 (필터링 및 정렬 적용)
        List<Product> products = productRepository.findProductsByCriteria(
            criteria.getMinPrice(),
            criteria.getMaxPrice(),
            criteria.getLocation(),
            criteria.getCategory(),
            criteria.getMinMannerScore(),
            criteria.getMinRating(),
            criteria.getSortBy(),
            criteria.getSortDirection()
        );
        
        // 상품 정보를 문자열로 변환
        StringBuilder productInfo = new StringBuilder();
        productInfo.append("현재 판매 중인 상품 목록:\n");
        for (Product product : products) {
            // 판매자의 매너 점수
            Double mannerScore = product.getUser().getMannerScore();
            
            // 상품의 평균 평점 계산
            Double averageRating = reviewRepository.getAverageRatingByProductId(product.getId());
            if (averageRating == null) averageRating = 0.0;
            
            productInfo.append(String.format("""
                    - 상품명: %s
                      카테고리: %s
                      가격: %s원 (AI 예측 가격 범위: %s~%s원)
                      상태: %s
                      위치: %s
                      판매자 매너점수: %.1f
                      상품 평점: %.1f
                      조회수: %d
                      채팅수: %d
                      이미지 수: %d
                      상세설명: %s
                    """,
                    product.getTitle(),
                    product.getCategory().getCategoryName(),
                    product.getPrice(),
                    product.getAiPriceMin(),
                    product.getAiPriceMax(),
                    product.getStatus(),
                    product.getLocationInfo(),
                    mannerScore,
                    averageRating,
                    product.getViewCount(),
                    product.getChatRooms().size(),
                    product.getImages().size(),
                    product.getDescription()));
        }
        
        // 사용자 요청과 상품 정보를 결합
        String prompt = String.format("""
                다음은 현재 판매 중인 상품 목록입니다:
                %s
                
                사용자 요청: %s
                
                위 상품 목록을 기반으로 사용자의 요청에 맞는 상품을 추천해주세요.
                다음 사항들을 고려하여 추천해주세요:
                1. 상품의 상태가 '판매중'인 상품을 우선적으로 추천
                2. 판매자의 매너점수가 높은 상품을 우선적으로 추천
                3. 상품의 평점이 높은 상품을 우선적으로 추천
                4. AI 예측 가격 범위를 참고하여 적절한 가격의 상품을 추천
                5. 조회수와 채팅수가 많은 상품은 인기 있는 상품일 수 있으므로 고려
                
                상품 목록에 없는 상품은 추천하지 마세요.
                """, productInfo.toString(), request.getUserRequest());

        return perplexityWebClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                    "model", "sonar-pro",
                    "messages", List.of(Map.of("role", "user", "content", prompt))
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String content = ((Map<String, Object>) ((List<Map<String, Object>>) response.get("choices")).get(0).get("message")).get("content").toString();
                    return new RecommendationResponse(content);
                });
    }
} 