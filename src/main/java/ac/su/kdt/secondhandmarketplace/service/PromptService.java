package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.ProductRecommendationDTO;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendation.RecommendationCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {

    /**
     * 추천 조건과 상품 목록을 기반으로 LLM용 프롬프트를 생성합니다.
     */
    public String generateRecommendationPrompt(RecommendationCriteria criteria, List<ProductRecommendationDTO> products) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("현재 판매중인 상품 중에서 다음 조건에 맞는 상품을 추천해주세요:\n");
        
        // 상품명 조건
        if (criteria.getProductName() != null) {
            prompt.append("1. 상품명: ").append(criteria.getProductName()).append("\n");
        }
        
        // 카테고리 조건
        if (criteria.getCategory() != null) {
            prompt.append("2. 카테고리: ").append(criteria.getCategory()).append("\n");
        }
        
        // 가격 범위 조건
        if (criteria.getMaxPrice() != null || criteria.getMinPrice() != null) {
            prompt.append("3. 가격 범위: ");
            if (criteria.getMinPrice() != null) {
                prompt.append(criteria.getMinPrice()).append("원");
            }
            prompt.append(" ~ ");
            if (criteria.getMaxPrice() != null) {
                prompt.append(criteria.getMaxPrice()).append("원");
            }
            prompt.append("\n");
        }
        
        // 상태 조건
        prompt.append("4. 상태: 판매중\n");
        
        // 품질 기준
        prompt.append("5. 품질 기준:\n");
        prompt.append("   - 매너 점수 ").append(criteria.getMinMannerScore()).append(" 이상\n");
        prompt.append("   - 리뷰 평점 ").append(criteria.getMinRating()).append(" 이상\n");
        
        // 위치 조건
        if (criteria.getLocation() != null) {
            prompt.append("6. 위치: ").append(criteria.getLocation()).append("\n");
        }
        
        // 정렬 기준
        if (criteria.getSortBy() != null) {
            prompt.append("7. 정렬: ").append(criteria.getSortBy())
                  .append(" ").append(criteria.getSortDirection()).append("\n");
        }
        
        // 상품 목록
        prompt.append("\n현재 판매중인 상품 목록:\n");
        for (ProductRecommendationDTO product : products) {
            prompt.append(formatProductInfo(product));
        }
        
        return prompt.toString();
    }

    /**
     * 상품 정보를 LLM이 이해하기 쉬운 형식으로 포맷팅합니다.
     */
    private String formatProductInfo(ProductRecommendationDTO product) {
        return String.format("[상품%d] %s\n" +
                           "- 가격: %d원\n" +
                           "- 상태: %s\n" +
                           "- 매너 점수: %.1f\n" +
                           "- 리뷰 평점: %.1f\n" +
                           "- 조회수: %d\n" +
                           "- AI 예측 가격: %d원 ~ %d원\n" +
                           "- 위치: %s\n" +
                           "- 판매자: %s (리뷰 %d개)\n" +
                           "- 설명: %s\n\n",
                           product.getId(),
                           product.getTitle(),
                           product.getPrice(),
                           product.getStatus(),
                           product.getMannerScore(),
                           product.getAverageRating(),
                           product.getViewCount(),
                           product.getAiPriceMin(),
                           product.getAiPriceMax(),
                           product.getLocationInfo(),
                           product.getSellerName(),
                           product.getSellerReviewCount(),
                           product.getDescription());
    }
} 