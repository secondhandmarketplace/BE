package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.PriceRecommendationRequest;
import ac.su.kdt.secondhandmarketplace.dto.PriceFactor;
import ac.su.kdt.secondhandmarketplace.dto.ProductRecommendationDTO;
import ac.su.kdt.secondhandmarketplace.dto.RecommendationCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import ac.su.kdt.secondhandmarketplace.entity.Product;

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

    /**
     * 가격 추천을 위한 프롬프트를 생성합니다.
     * 
     * @param request 가격 추천 요청 정보
     * @param factors 가격 결정 요소 목록
     * @return 생성된 프롬프트
     */
    public String generatePriceRecommendationPrompt(PriceRecommendationRequest request, List<PriceFactor> factors) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 상품의 적정가를 추천하기 위한 데이터입니다:\n\n");
        
        // 사용자 요청 정보
        prompt.append("1. 사용자 요청:\n");
        prompt.append("- 상품: ").append(request.getUserRequest()).append("\n");
        prompt.append("- 카테고리: ").append(request.getCategory()).append("\n");
        prompt.append("- 상태: ").append(request.getCondition()).append("\n");
        if (request.getCurrentPrice() != null) {
            prompt.append("- 현재 가격: ").append(request.getCurrentPrice()).append("원\n");
        }
        prompt.append("\n");

        // 내부 플랫폼 데이터
        prompt.append("2. 내부 플랫폼 데이터:\n");
        factors.stream()
            .filter(f -> "INTERNAL".equals(f.getDataSource()))
            .forEach(f -> {
                prompt.append("- ").append(f.getFactorName()).append("\n");
                prompt.append("  ").append(f.getDescription()).append("\n");
                if (f.getImpact() != null) {
                    prompt.append("  영향도: ").append(f.getImpact().multiply(new BigDecimal("100"))).append("%\n");
                }
                prompt.append("\n");
            });

        // 외부 사이트 데이터
        prompt.append("3. 외부 중고거래 사이트 데이터:\n");
        factors.stream()
            .filter(f -> "EXTERNAL".equals(f.getDataSource()))
            .forEach(f -> {
                prompt.append("- ").append(f.getFactorName()).append("\n");
                prompt.append("  ").append(f.getDescription()).append("\n");
                if (f.getImpact() != null) {
                    prompt.append("  영향도: ").append(f.getImpact().multiply(new BigDecimal("100"))).append("%\n");
                }
                prompt.append("\n");
            });

        // AI에게 요청
        prompt.append("위 데이터를 기반으로 다음 정보를 제공해주세요:\n\n");
        prompt.append("1. 추천 가격 범위\n");
        prompt.append("   - 최소 가격: (숫자만 입력)\n");
        prompt.append("   - 최대 가격: (숫자만 입력)\n");
        prompt.append("   - 평균 가격: (숫자만 입력)\n\n");
        
        prompt.append("2. 가격 결정 근거\n");
        prompt.append("   - 내부 플랫폼 데이터 분석 결과\n");
        prompt.append("   - 외부 시장 데이터 분석 결과\n");
        prompt.append("   - 시장 동향과 가격 변동 요인\n\n");
        
        prompt.append("3. 가격 영향 요소 분석\n");
        prompt.append("   - 각 요소가 가격에 미친 영향\n");
        prompt.append("   - 주요 가격 결정 요소\n\n");
        
        if (request.getCurrentPrice() != null) {
            prompt.append("4. 가격 조정 제안\n");
            prompt.append("   - 현재 가격과의 비교\n");
            prompt.append("   - 조정이 필요한 경우 그 이유\n\n");
        }
        
        prompt.append("5. 시장 전략 제안\n");
        prompt.append("   - 현재 시장 상황 분석\n");
        prompt.append("   - 가격 책정 전략\n");
        prompt.append("   - 판매 시기와 방법에 대한 제안\n");

        return prompt.toString();
    }
} 