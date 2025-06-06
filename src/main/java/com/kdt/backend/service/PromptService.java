package com.kdt.backend.service;

import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.RecommendationCriteria;
import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceFactor;
import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptService {

    private final ItemRepository itemRepository;

    /**
     * ✅ 실제 데이터베이스 기반 상품 추천 응답 생성 (AI 없이)
     */
    public String generateRecommendationResponse(String userRequest) {
        try {
            log.info("상품 추천 요청: {}", userRequest);

            // 1. 최저가 요청 처리
            if (containsCheapestKeywords(userRequest)) {
                return generateRealCheapestItemResponse();
            }

            // 2. 최고가 요청 처리
            if (containsExpensiveKeywords(userRequest)) {
                return generateRealMostExpensiveItemResponse();
            }

            // 3. 카테고리별 요청 처리
            String category = extractCategory(userRequest);
            if (category != null) {
                return generateRealCategoryResponse(category);
            }

            // 4. 일반 키워드 검색
            return generateRealKeywordSearchResponse(userRequest);

        } catch (Exception e) {
            log.error("상품 추천 응답 생성 실패: {}", e.getMessage());
            return "죄송해요, 상품 정보를 가져오는 중 오류가 발생했습니다.";
        }
    }

    /**
     * ✅ 가격 추천 전용 응답 생성 (모든 상품 지원)
     */
    public String generatePriceRecommendationResponse(String userRequest) {
        try {
            log.info("가격 추천 요청: {}", userRequest);

            if (containsPriceRecommendationKeywords(userRequest)) {
                return generateUniversalPriceRecommendation(userRequest);
            }

            return generateRecommendationResponse(userRequest);

        } catch (Exception e) {
            log.error("가격 추천 응답 생성 실패: {}", e.getMessage());
            return "죄송해요, 가격 추천 중 오류가 발생했습니다.";
        }
    }

    /**
     * ✅ 범용 가격 추천 (모든 상품 지원)
     */
    private String generateUniversalPriceRecommendation(String userRequest) {
        String productInfo = extractProductInfo(userRequest);
        String condition = extractConditionFromRequest(userRequest);

        log.info("추출된 상품 정보: {}, 상태: {}", productInfo, condition);

        // 유사 상품 검색
        List<Item> similarItems = findSimilarItemsForPricing(productInfo);

        if (!similarItems.isEmpty()) {
            return generatePriceRecommendationFromSimilarItems(productInfo, condition, similarItems);
        } else {
            return generateGeneralPriceRecommendation(productInfo, condition, userRequest);
        }
    }

    /**
     * ✅ 유사 상품 기반 가격 추천
     */
    private String generatePriceRecommendationFromSimilarItems(String productInfo, String condition, List<Item> similarItems) {
        int minPrice = similarItems.stream().mapToInt(Item::getPrice).min().orElse(0);
        int maxPrice = similarItems.stream().mapToInt(Item::getPrice).max().orElse(0);
        double avgPrice = similarItems.stream().mapToInt(Item::getPrice).average().orElse(0);

        return String.format(
                "%s (%s 상태)의 추천 가격은 %,d원 ~ %,d원입니다! " +
                        "우리 플랫폼의 유사 상품 %d개를 분석한 결과, 평균 가격은 %,d원이에요.",
                productInfo, condition, minPrice, maxPrice, similarItems.size(), (int) avgPrice
        );
    }

    /**
     * ✅ 일반적인 가격 추천 (모든 상품 지원)
     */
    private String generateGeneralPriceRecommendation(String productInfo, String condition, String userRequest) {
        // 상품 유형별 가격 가이드
        if (isElectronics(productInfo)) {
            return generateElectronicsPriceRecommendation(productInfo, condition, userRequest);
        } else if (isClothing(productInfo)) {
            return generateClothingPriceRecommendation(productInfo, condition);
        } else if (isBook(productInfo)) {
            return generateBookPriceRecommendation(productInfo, condition);
        } else if (isFurniture(productInfo)) {
            return generateFurniturePriceRecommendation(productInfo, condition);
        } else {
            return generateDefaultPriceRecommendation(productInfo, condition);
        }
    }

    /**
     * ✅ 전자제품 가격 추천
     */
    private String generateElectronicsPriceRecommendation(String productInfo, String condition, String userRequest) {
        if (productInfo.toLowerCase().contains("아이폰")) {
            return generateiPhonePriceRecommendation(productInfo, condition, userRequest);
        } else if (productInfo.toLowerCase().contains("갤럭시")) {
            return generateGalaxyPriceRecommendation(productInfo, condition);
        } else if (productInfo.toLowerCase().contains("노트북")) {
            return generateLaptopPriceRecommendation(productInfo, condition);
        } else {
            return String.format(
                    "%s (%s 상태)의 경우 브랜드와 모델에 따라 가격이 크게 달라져요. " +
                            "정확한 모델명을 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                    productInfo, condition
            );
        }
    }

    /**
     * ✅ 아이폰 가격 추천
     */
    private String generateiPhonePriceRecommendation(String productInfo, String condition, String userRequest) {
        if (userRequest.contains("12 프로") || userRequest.contains("12프로")) {
            int basePrice = getBasePrice("아이폰 12 프로", condition);
            int minPrice = (int) (basePrice * 0.85);
            int maxPrice = (int) (basePrice * 1.15);

            return String.format(
                    "아이폰 12 프로 (%s)의 추천 가격은 %,d원 ~ %,d원입니다! " +
                            "현재 중고 시장에서 평균 %,d원 정도에 거래되고 있어요.",
                    condition, minPrice, maxPrice, basePrice
            );
        } else if (userRequest.contains("13")) {
            return "아이폰 13 시리즈의 경우 모델과 용량에 따라 가격이 달라져요. 정확한 모델명을 알려주세요.";
        } else {
            return "아이폰 가격은 모델과 상태에 따라 차이가 크니, 정확한 모델명을 알려주시면 더 정확한 추천을 드릴 수 있어요.";
        }
    }

    /**
     * ✅ 갤럭시 가격 추천
     */
    private String generateGalaxyPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 모델과 출시년도에 따라 가격이 달라져요. " +
                        "갤럭시 S 시리즈인지 노트 시리즈인지 정확한 모델명을 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                productInfo, condition
        );
    }

    /**
     * ✅ 노트북 가격 추천
     */
    private String generateLaptopPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 브랜드, 사양, 사용 기간에 따라 가격이 크게 달라져요. " +
                        "CPU, RAM, 저장용량 등의 사양 정보를 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                productInfo, condition
        );
    }

    /**
     * ✅ 의류 가격 추천
     */
    private String generateClothingPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 브랜드와 원가에 따라 가격이 달라져요. " +
                        "명품 브랜드의 경우 원가의 30-50%%, 일반 브랜드는 20-40%% 정도가 적정 가격대입니다.",
                productInfo, condition
        );
    }

    /**
     * ✅ 도서 가격 추천
     */
    private String generateBookPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 정가의 50-70%% 정도가 적정 가격대예요. " +
                        "인기 도서나 절판된 책의 경우 더 높은 가격에 거래될 수 있습니다.",
                productInfo, condition
        );
    }

    /**
     * ✅ 가구 가격 추천
     */
    private String generateFurniturePriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 경우 브랜드, 크기, 재질에 따라 가격이 달라져요. " +
                        "일반적으로 원가의 20-40%% 정도가 적정 가격대입니다.",
                productInfo, condition
        );
    }

    /**
     * ✅ 기본 가격 추천
     */
    private String generateDefaultPriceRecommendation(String productInfo, String condition) {
        return String.format(
                "%s (%s 상태)의 가격 추천을 위해서는 더 많은 정보가 필요해요. " +
                        "브랜드, 모델, 구매 시기 등을 알려주시면 더 정확한 가격을 추천드릴 수 있습니다.",
                productInfo, condition
        );
    }

    // ===== 헬퍼 메서드들 =====

    /**
     * 상품 유형 판별 메서드들
     */
    private boolean isElectronics(String productInfo) {
        String[] electronics = {"아이폰", "갤럭시", "노트북", "컴퓨터", "태블릿", "이어폰", "스마트워치"};
        String lower = productInfo.toLowerCase();
        for (String item : electronics) {
            if (lower.contains(item)) return true;
        }
        return false;
    }

    private boolean isClothing(String productInfo) {
        String[] clothing = {"옷", "셔츠", "바지", "치마", "자켓", "코트", "신발", "가방"};
        String lower = productInfo.toLowerCase();
        for (String item : clothing) {
            if (lower.contains(item)) return true;
        }
        return false;
    }

    private boolean isBook(String productInfo) {
        String[] books = {"책", "도서", "소설", "교재", "참고서", "만화"};
        String lower = productInfo.toLowerCase();
        for (String item : books) {
            if (lower.contains(item)) return true;
        }
        return false;
    }

    private boolean isFurniture(String productInfo) {
        String[] furniture = {"책상", "의자", "침대", "소파", "장롱", "서랍", "테이블"};
        String lower = productInfo.toLowerCase();
        for (String item : furniture) {
            if (lower.contains(item)) return true;
        }
        return false;
    }

    /**
     * 기본 가격 계산
     */
    private int getBasePrice(String model, String condition) {
        if (model.contains("아이폰 12 프로")) {
            switch (condition.toUpperCase()) {
                case "S급": return 750000;
                case "A급": return 650000;
                case "B급": return 550000;
                case "C급": return 450000;
                default: return 600000;
            }
        }
        return 300000; // 기본값
    }

    /**
     * 가격 책정용 유사 상품 검색
     */
    private List<Item> findSimilarItemsForPricing(String productInfo) {
        try {
            String[] keywords = productInfo.toLowerCase().split("\\s+");
            List<Item> allItems = itemRepository.findByStatus(Item.Status.판매중);

            return allItems.stream()
                    .filter(item -> {
                        String title = item.getTitle().toLowerCase();
                        for (String keyword : keywords) {
                            if (title.contains(keyword)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("유사 상품 검색 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 상품 정보 추출 (범용)
     */
    private String extractProductInfo(String userRequest) {
        String lower = userRequest.toLowerCase();

        // 전자제품
        if (lower.contains("아이폰") || lower.contains("iphone")) {
            if (lower.contains("12") && (lower.contains("프로") || lower.contains("pro"))) {
                return "아이폰 12 프로";
            } else if (lower.contains("13")) {
                return "아이폰 13";
            } else if (lower.contains("12")) {
                return "아이폰 12";
            }
            return "아이폰";
        } else if (lower.contains("갤럭시")) {
            return "갤럭시";
        } else if (lower.contains("노트북")) {
            return "노트북";
        }
        // 의류
        else if (lower.contains("옷") || lower.contains("셔츠") || lower.contains("바지")) {
            return "의류";
        }
        // 도서
        else if (lower.contains("책") || lower.contains("도서")) {
            return "도서";
        }
        // 가구
        else if (lower.contains("책상") || lower.contains("의자") || lower.contains("침대")) {
            return "가구";
        }

        return "상품";
    }

    /**
     * 상태 정보 추출
     */
    private String extractConditionFromRequest(String userRequest) {
        String lower = userRequest.toLowerCase();

        if (lower.contains("s급") || lower.contains("s등급")) return "S급";
        if (lower.contains("a급") || lower.contains("a등급")) return "A급";
        if (lower.contains("b급") || lower.contains("b등급")) return "B급";
        if (lower.contains("c급") || lower.contains("c등급")) return "C급";

        return "중고";
    }

    /**
     * 키워드 확인 메서드들
     */
    private boolean containsPriceRecommendationKeywords(String userRequest) {
        String[] priceKeywords = {"얼마에 팔", "가격", "얼마", "시세", "추천", "적정가", "판매가", "얼마에 파는게"};
        String lower = userRequest.toLowerCase();

        for (String keyword : priceKeywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private boolean containsCheapestKeywords(String userRequest) {
        String[] cheapestKeywords = {"가장 싼", "최저가", "제일 싼", "가장 저렴한", "제일 저렴한", "싼 것"};
        String lower = userRequest.toLowerCase();

        for (String keyword : cheapestKeywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private boolean containsExpensiveKeywords(String userRequest) {
        String[] expensiveKeywords = {"가장 비싼", "최고가", "제일 비싼", "비싼 것"};
        String lower = userRequest.toLowerCase();

        for (String keyword : expensiveKeywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    // ===== 상품 추천 메서드들 =====

    private String generateRealCheapestItemResponse() {
        try {
            List<Item> cheapestItems = itemRepository.findByStatusAndPriceGreaterThanOrderByPriceAsc(
                    Item.Status.판매중, 0);

            if (!cheapestItems.isEmpty()) {
                Item cheapest = cheapestItems.get(0);
                return String.format(
                        "우리 플랫폼에서 가장 저렴한 상품은 '%s'이고 가격은 %,d원입니다!",
                        cheapest.getTitle(), cheapest.getPrice()
                );
            } else {
                return "현재 등록된 상품이 없습니다.";
            }
        } catch (Exception e) {
            log.error("최저가 상품 조회 실패: {}", e.getMessage());
            return "최저가 상품을 찾는 중 오류가 발생했습니다.";
        }
    }

    private String generateRealMostExpensiveItemResponse() {
        try {
            List<Item> expensiveItems = itemRepository.findByStatusOrderByPriceDesc(Item.Status.판매중);

            if (!expensiveItems.isEmpty()) {
                Item mostExpensive = expensiveItems.get(0);
                return String.format(
                        "우리 플랫폼에서 가장 비싼 상품은 '%s'이고 가격은 %,d원입니다!",
                        mostExpensive.getTitle(), mostExpensive.getPrice()
                );
            } else {
                return "현재 등록된 상품이 없습니다.";
            }
        } catch (Exception e) {
            log.error("최고가 상품 조회 실패: {}", e.getMessage());
            return "최고가 상품을 찾는 중 오류가 발생했습니다.";
        }
    }

    private String generateRealCategoryResponse(String category) {
        try {
            List<Item> categoryItems = itemRepository.findByCategoryAndStatus(category, Item.Status.판매중);

            if (!categoryItems.isEmpty()) {
                Item cheapestInCategory = categoryItems.stream()
                        .min((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                        .orElse(categoryItems.get(0));

                return String.format(
                        "%s 카테고리에서 가장 저렴한 상품은 '%s'이고 가격은 %,d원입니다!",
                        category, cheapestInCategory.getTitle(), cheapestInCategory.getPrice()
                );
            } else {
                return String.format("죄송해요, %s 카테고리에는 현재 등록된 상품이 없습니다.", category);
            }
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 실패: {}", e.getMessage());
            return category + " 카테고리 상품을 찾는 중 오류가 발생했습니다.";
        }
    }

    private String generateRealKeywordSearchResponse(String userRequest) {
        try {
            List<Item> allItems = itemRepository.findByStatus(Item.Status.판매중);

            if (allItems.isEmpty()) {
                return "현재 등록된 상품이 없습니다.";
            }

            List<Item> matchedItems = findRealMatchingItems(allItems, userRequest);

            if (!matchedItems.isEmpty()) {
                Item bestMatch = matchedItems.get(0);
                return String.format(
                        "요청하신 조건에 맞는 상품을 찾았어요! '%s' - %,d원입니다.",
                        bestMatch.getTitle(), bestMatch.getPrice()
                );
            } else {
                Item latestItem = allItems.stream()
                        .max((a, b) -> a.getRegDate().compareTo(b.getRegDate()))
                        .orElse(allItems.get(0));

                return String.format(
                        "요청하신 조건에 정확히 맞는 상품은 없지만, 최근에 등록된 '%s' - %,d원 상품을 추천드려요!",
                        latestItem.getTitle(), latestItem.getPrice()
                );
            }
        } catch (Exception e) {
            log.error("키워드 검색 실패: {}", e.getMessage());
            return "상품 검색 중 오류가 발생했습니다.";
        }
    }

    private List<Item> findRealMatchingItems(List<Item> allItems, String userRequest) {
        String[] keywords = userRequest.toLowerCase().split("\\s+");

        return allItems.stream()
                .filter(item -> {
                    String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                    String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                    String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";

                    for (String keyword : keywords) {
                        if (title.contains(keyword) || category.contains(keyword) || description.contains(keyword)) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                .collect(Collectors.toList());
    }

    private String extractCategory(String userRequest) {
        String[] categories = {"전자기기", "의류", "도서", "생활용품", "가구", "전자제품", "기타"};
        String lowerRequest = userRequest.toLowerCase();

        for (String category : categories) {
            if (lowerRequest.contains(category)) {
                return category;
            }
        }
        return null;
    }

    // ===== 기존 프롬프트 메서드들 (가격 추천용) =====

    public String generatePricePrompt(PriceRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 우리 중고거래 플랫폼의 AI 가격 전문가입니다.\n");
        prompt.append("외부 중고거래 사이트의 시세 정보를 참고하여 가격을 분석해도 됩니다.\n");
        prompt.append("하지만 상품 목록이나 게시글은 우리 플랫폼 데이터만 사용해주세요.\n");

        prompt.append("1. 사용자 요청: ").append(request.getUserRequest()).append("\n");
        prompt.append("- 카테고리: ").append(request.getCategory()).append("\n");
        prompt.append("- 상태: ").append(request.getCondition()).append("\n");

        prompt.append("\n위 정보를 바탕으로 적정 가격 범위를 추천해주세요.");
        return prompt.toString();
    }

    public String generateRecommendationPrompt(String userRequest) {
        return generateRecommendationResponse(userRequest);
    }

    public String generateRecommendationPrompt(RecommendationCriteria criteria, List<ItemResponseDTO> products, String userRequest) {
        if (products == null || products.isEmpty()) {
            return "현재 등록된 상품이 없습니다.";
        }

        if (containsCheapestKeywords(userRequest)) {
            ItemResponseDTO cheapest = products.stream()
                    .min((a, b) -> a.getPrice().compareTo(b.getPrice()))
                    .orElse(products.get(0));

            return String.format("현재 등록된 상품 중에서 가장 저렴한 것은 '%s'이고 가격은 %s원입니다!",
                    cheapest.getTitle(), cheapest.getPrice());
        }

        if (containsExpensiveKeywords(userRequest)) {
            ItemResponseDTO mostExpensive = products.stream()
                    .max((a, b) -> a.getPrice().compareTo(b.getPrice()))
                    .orElse(products.get(0));

            return String.format("현재 등록된 상품 중에서 가장 비싼 것은 '%s'이고 가격은 %s원입니다!",
                    mostExpensive.getTitle(), mostExpensive.getPrice());
        }

        return "요청하신 조건에 맞는 상품을 찾지 못했습니다.";
    }

    public String generatePriceRecommendationPrompt(PriceRecommendationRequest request, List<PriceFactor> factors) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 우리 중고거래 플랫폼의 AI 가격 전문가입니다.\n");
        prompt.append("외부 중고거래 사이트의 시세 정보를 참고하여 가격 분석을 해도 됩니다.\n");
        prompt.append("하지만 상품 목록이나 게시글은 우리 플랫폼 데이터만 언급해주세요.\n");

        prompt.append("1. 사용자 요청: ").append(request.getUserRequest()).append("\n");
        prompt.append("- 카테고리: ").append(request.getCategory()).append("\n");
        prompt.append("- 상태: ").append(request.getCondition()).append("\n");

        prompt.append("2. 우리 플랫폼 데이터 분석:\n");
        factors.stream()
                .filter(f -> "INTERNAL".equals(f.getDataSource()))
                .forEach(f -> prompt.append("- ").append(f.getDescription()).append("\n"));

        prompt.append("\n위 데이터를 바탕으로 적정 가격 범위를 추천해주세요.");
        return prompt.toString();
    }
}
