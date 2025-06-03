package com.kdt.backend.controller;

import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.service.PerplexityService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final PerplexityService perplexityService;
    private final ItemRepository itemRepository;

    private boolean isSmallTalkOrIntro(String question) {
        String q = question.toLowerCase().trim();
        String[] patterns = {
                "안녕", "안녕하세요", "고마워", "감사", "반가워", "수고", "잘자", "잘 지내", "소개", "누구", "정체", "무엇을 할 수", "뭐 할 수", "뭐가 가능", "역할", "설명"
        };
        for (String p : patterns) {
            if (q.contains(p)) return true;
        }
        return false;
    }

    @GetMapping("/ask")
    public Mono<Map<String, Object>> ask(@RequestParam String question) {
        if (isSmallTalkOrIntro(question)) {
            Map<String, Object> result = new HashMap<>();
            result.put("answer", "안녕하세요! 저는 중고거래 플랫폼의 AI 어시스턴트입니다. 상품 추천, 가격 비교, 조건별 검색 등 다양한 중고거래 관련 도움을 드릴 수 있어요. 궁금한 점이나 원하는 상품을 말씀해 주세요!");
            result.put("mainItem", null);
            result.put("recommendedItems", List.of());
            return Mono.just(result);
        }

        return perplexityService.getRecommendations(question)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("answer", response.getContent());

                    // 고도화된 아이템 분석 및 추천
                    Map<String, Object> itemData = getAdvancedRecommendations(question);
                    result.put("mainItem", itemData.get("mainItem"));
                    result.put("recommendedItems", itemData.get("recommendedItems"));
                    result.put("searchType", itemData.get("searchType"));

                    return result;
                });
    }

    /**
     * 고도화된 추천 시스템 - 다양한 질문 패턴 분석
     */
    private Map<String, Object> getAdvancedRecommendations(String question) {
        try {
            List<Item> allItems = itemRepository.findAllWithImages();
            String normalizedQuestion = question.toLowerCase().trim();

            System.out.println("=== 질문 분석: " + question);

            // 질문 분석 결과
            QueryAnalysis analysis = analyzeQuery(normalizedQuestion);
            System.out.println("=== 분석 결과: " + analysis);

            // 분석 결과에 따른 필터링 및 정렬
            List<Item> filteredItems = applyFiltersAndSorting(allItems, analysis);

            Map<String, Object> result = new HashMap<>();

            // 메인 아이템 선택
            if (!filteredItems.isEmpty()) {
                Item mainItem = selectMainItem(filteredItems, analysis, question);
                assert mainItem != null;
                result.put("mainItem", convertToItemCard(mainItem));
                System.out.println("=== 메인 아이템: " + mainItem.getTitle());

                // 연관 추천 아이템
                List<Item> relatedItems = getRelatedItems(mainItem, allItems, analysis);
                List<Map<String, Object>> recommendedItems = relatedItems.stream()
                        .limit(3)
                        .map(this::convertToItemCard)
                        .collect(Collectors.toList());

                result.put("recommendedItems", recommendedItems);
            } else {
                result.put("mainItem", null);
                result.put("recommendedItems", getRandomItems(allItems, 3));
            }

            result.put("searchType", analysis.getSearchType());
            System.out.println("=== 검색 타입: " + analysis.getSearchType());

            return result;

        } catch (Exception e) {
            System.err.println("=== 추천 시스템 오류: " + e.getMessage());
            e.printStackTrace();
            return Map.of("mainItem", null, "recommendedItems", List.of(), "searchType", "error");
        }
    }

    /**
     * 질문 분석 클래스
     */
    @Getter
    @Setter
    private static class QueryAnalysis {
        private String searchType;
        private String category;
        private String sortBy;
        private String sortDirection;
        private Integer minPrice;
        private Integer maxPrice;
        private List<String> keywords;
        private boolean isComparison;
        private boolean isRecommendation;

        @Override
        public String toString() {
            return String.format("QueryAnalysis{searchType='%s', category='%s', sortBy='%s', keywords=%s}",
                    searchType, category, sortBy, keywords);
        }
    }

    /**
     * 고도화된 질문 분석
     */
    private QueryAnalysis analyzeQuery(String question) {
        QueryAnalysis analysis = new QueryAnalysis();
        analysis.setKeywords(new ArrayList<>());

        // 1. 가격 관련 분석
        if (question.contains("비싼") || question.contains("높은") || question.contains("최고가") || question.contains("고가")) {
            analysis.setSearchType("price_high");
            analysis.setSortBy("price");
            analysis.setSortDirection("desc");
        } else if (question.contains("싼") || question.contains("저렴") || question.contains("최저가") || question.contains("저가")) {
            analysis.setSearchType("price_low");
            analysis.setSortBy("price");
            analysis.setSortDirection("asc");
        }

        // 2. 카테고리 분석
        Map<String, List<String>> categoryKeywords = Map.of(
                "전자제품", Arrays.asList("노트북", "컴퓨터", "아이폰", "갤럭시", "스마트폰", "태블릿", "이어폰", "헤드폰"),
                "가구", Arrays.asList("의자", "책상", "침대", "소파", "장롱", "서랍"),
                "의류", Arrays.asList("옷", "바지", "셔츠", "원피스", "자켓", "코트"),
                "도서", Arrays.asList("책", "소설", "만화", "교재", "참고서"),
                "생활용품", Arrays.asList("청소기", "세탁기", "냉장고", "전자레인지", "에어컨"),
                "스포츠/레저", Arrays.asList("운동", "헬스", "자전거", "골프", "축구", "농구")
        );

        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (question.contains(keyword)) {
                    analysis.setCategory(entry.getKey());
                    analysis.getKeywords().add(keyword);
                    if (analysis.getSearchType() == null) {
                        analysis.setSearchType("category");
                    }
                    break;
                }
            }
        }

        // 3. 가격 범위 분석
        Pattern pricePattern = Pattern.compile("(\\d+)만원?\\s*(이하|미만|이상|초과)");
        Matcher priceMatcher = pricePattern.matcher(question);
        while (priceMatcher.find()) {
            int price = Integer.parseInt(priceMatcher.group(1)) * 10000;
            String condition = priceMatcher.group(2);

            if (condition.equals("이하") || condition.equals("미만")) {
                analysis.setMaxPrice(price);
            } else if (condition.equals("이상") || condition.equals("초과")) {
                analysis.setMinPrice(price);
            }
        }

        // 4. 추천 요청 분석
        if (question.contains("추천") || question.contains("recommend") || question.contains("좋은") || question.contains("괜찮은")) {
            analysis.setRecommendation(true);
            if (analysis.getSearchType() == null) {
                analysis.setSearchType("recommendation");
            }
        }

        // 5. 비교 요청 분석
        if (question.contains("비교") || question.contains("차이") || question.contains("vs") || question.contains("대비")) {
            analysis.setComparison(true);
            if (analysis.getSearchType() == null) {
                analysis.setSearchType("comparison");
            }
        }

        // 6. 기본값 설정
        if (analysis.getSearchType() == null) {
            analysis.setSearchType("general");
            analysis.setSortBy("date");
            analysis.setSortDirection("desc");
        }

        return analysis;
    }

    /**
     * 필터링 및 정렬 적용
     */
    private List<Item> applyFiltersAndSorting(List<Item> items, QueryAnalysis analysis) {
        return items.stream()
                // 카테고리 필터
                .filter(item -> analysis.getCategory() == null ||
                        (item.getCategory() != null && item.getCategory().equals(analysis.getCategory())))
                // 키워드 필터
                .filter(item -> analysis.getKeywords().isEmpty() ||
                        analysis.getKeywords().stream().anyMatch(keyword ->
                                item.getTitle().toLowerCase().contains(keyword)))
                // 가격 필터
                .filter(item -> analysis.getMinPrice() == null || item.getPrice() >= analysis.getMinPrice())
                .filter(item -> analysis.getMaxPrice() == null || item.getPrice() <= analysis.getMaxPrice())
                // 정렬
                .sorted((a, b) -> {
                    if ("price".equals(analysis.getSortBy())) {
                        int comparison = Integer.compare(a.getPrice(), b.getPrice());
                        return "desc".equals(analysis.getSortDirection()) ? -comparison : comparison;
                    } else {
                        return b.getRegDate().compareTo(a.getRegDate()); // 기본: 최신순
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 메인 아이템 선택 로직
     */
    private Item selectMainItem(List<Item> items, QueryAnalysis analysis, String question) {
        if (items.isEmpty()) return null;

        int index = 0;
        if (question.contains("두 번째") || question.contains("2번째") || question.contains("2번")) {
            index = 1;
        } else if (question.contains("세 번째") || question.contains("3번째") || question.contains("3번")) {
            index = 2;
        }
        if (index >= items.size()) index = 0;
        return items.get(index);
    }

    /**
     * 연관 상품 추천 (고도화)
     */
    private List<Item> getRelatedItems(Item mainItem, List<Item> allItems, QueryAnalysis analysis) {
        List<Item> candidates = allItems.stream()
                .filter(item -> !item.getItemid().equals(mainItem.getItemid()))
                .collect(Collectors.toList());

        // 1. 같은 카테고리 우선
        List<Item> sameCategory = candidates.stream()
                .filter(item -> mainItem.getCategory() != null &&
                        mainItem.getCategory().equals(item.getCategory()))
                .collect(Collectors.toList());

        // 2. 비슷한 가격대 (±30%)
        int priceRange = (int) (mainItem.getPrice() * 0.3);
        List<Item> similarPrice = candidates.stream()
                .filter(item -> Math.abs(item.getPrice() - mainItem.getPrice()) <= priceRange)
                .collect(Collectors.toList());

        // 3. 우선순위: 같은 카테고리 + 비슷한 가격 > 같은 카테고리 > 비슷한 가격 > 랜덤
        List<Item> result = new ArrayList<>();

        // 같은 카테고리 + 비슷한 가격
        List<Item> bestMatch = sameCategory.stream()
                .filter(similarPrice::contains)
                .collect(Collectors.toList());
        Collections.shuffle(bestMatch);
        result.addAll(bestMatch);

        if (result.size() < 3) {
            Collections.shuffle(sameCategory);
            result.addAll(sameCategory.stream()
                    .filter(item -> !result.contains(item))
                    .limit(3 - result.size())
                    .collect(Collectors.toList()));
        }

        if (result.size() < 3) {
            Collections.shuffle(similarPrice);
            result.addAll(similarPrice.stream()
                    .filter(item -> !result.contains(item))
                    .limit(3 - result.size())
                    .collect(Collectors.toList()));
        }

        if (result.size() < 3) {
            Collections.shuffle(candidates);
            result.addAll(candidates.stream()
                    .filter(item -> !result.contains(item))
                    .limit(3 - result.size())
                    .collect(Collectors.toList()));
        }

        return result;
    }

    /**
     * 랜덤 아이템 선택
     */
    private List<Map<String, Object>> getRandomItems(List<Item> items, int count) {
        Collections.shuffle(items);
        return items.stream()
                .limit(count)
                .map(this::convertToItemCard)
                .collect(Collectors.toList());
    }

    /**
     * 아이템을 카드 형태로 변환
     */
    private Map<String, Object> convertToItemCard(Item item) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", item.getItemid());
        card.put("title", item.getTitle() != null ? item.getTitle() : "제목 없음");
        card.put("price", item.getPrice());
        card.put("description", item.getDescription() != null ? item.getDescription() : "설명 없음");
        card.put("location", item.getMeetLocation() != null ? item.getMeetLocation() : "위치 미정");
        card.put("category", item.getCategory());
        card.put("viewCount", item.getViewCount() != null ? item.getViewCount() : 0);
        System.out.println("convertToItemCard: itemId=" + item.getItemid() + ", images=" + item.getItemImages());
        if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
            String imagePath = item.getItemImages().get(0).getPhotoPath();
            System.out.println("이미지 경로 반환: " + imagePath);
            card.put("image", "http://localhost:8080" + imagePath);
        } else {
            System.out.println("이미지 없음, default 반환");
            card.put("image", "/assets/default-image.png");
        }
        // 이미지 처리
        try {
            if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                String imagePath = item.getItemImages().get(0).getPhotoPath();
                card.put("image", "http://localhost:8080" + imagePath);
            } else {
                card.put("image", "/assets/default-image.png");
            }
        } catch (Exception e) {
            card.put("image", "/assets/default-image.png");
            System.err.println("=== 이미지 처리 오류: " + e.getMessage());
        }

        return card;
    }

    /**
     * 테스트용 엔드포인트
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "message", "AI 추천 시스템이 정상 동작합니다",
                "timestamp", System.currentTimeMillis(),
                "supportedQueries", Arrays.asList(
                        "제일 비싼 노트북 찾아줘",
                        "10만원 이하 청소기 추천해줘",
                        "가성비 좋은 전자제품 보여줘",
                        "최신 스마트폰 비교해줘"
                )
        );
    }
}
