package com.kdt.backend.service;

import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.RecommendationCriteria;
import com.kdt.backend.dto.PriceRecommendationRequest;
import com.kdt.backend.dto.PriceFactor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {

    /**
     * [상품 추천] 사용자 요청 기반 프롬프트 생성
     * (간단하게 상품 목록과 사용자 요청을 받아 자연스러운 추천 프롬프트 생성)
     */
    public String generateRecommendationPrompt(String userRequest) {
        // 실제로는 상품 목록 등 더 많은 정보를 넣을 수 있음
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 중고거래 플랫폼의 AI 어시스턴트입니다.\n");
        prompt.append("사용자의 요청에 친근하고 자연스럽게 답변해주세요.\n");
        prompt.append("SQL 명령어나 기술적인 내용은 포함하지 마세요.\n\n");
        prompt.append("사용자 요청: ").append(userRequest).append("\n\n");
        prompt.append("위 요청에 맞는 상품을 추천해주세요.\n");
        prompt.append("답변은 친근하고 간단하게 해주세요. SQL이나 기술적 설명은 포함하지 마세요.\n");
        prompt.append("예시: '가장 저렴한 상품은 [상품명]이고 가격은 XX원입니다!'");
        return prompt.toString();
    }

    /**
     * [상품 추천] (옵션) 상품 목록까지 포함한 프롬프트 생성
     */
    public String generateRecommendationPrompt(RecommendationCriteria criteria, List<ItemResponseDTO> products, String userRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 중고거래 플랫폼의 AI 어시스턴트입니다.\n");
        prompt.append("사용자의 요청에 친근하고 자연스럽게 답변해주세요.\n");
        prompt.append("SQL 명령어나 기술적인 내용은 포함하지 마세요.\n\n");

        prompt.append("현재 판매중인 상품 목록:\n");
        for (ItemResponseDTO product : products) {
            if (product != null) {
                prompt.append(String.format("[상품%s] %s - %s원\n",
                        product.getId() != null ? product.getId() : "미정",
                        product.getTitle() != null ? product.getTitle() : "제목 없음",
                        product.getPrice() != null ? product.getPrice() : "가격 미정"));
            }
        }

        prompt.append("\n사용자 요청: ").append(userRequest).append("\n\n");
        prompt.append("위 상품 목록을 바탕으로 사용자의 요청에 맞는 상품을 추천해주세요.\n");
        prompt.append("답변은 친근하고 간단하게 해주세요. SQL이나 기술적 설명은 포함하지 마세요.\n");
        prompt.append("예시: '가장 저렴한 상품은 [상품명]이고 가격은 XX원입니다!'");
        return prompt.toString();
    }

    /**
     * [가격 추천] 가격 추천 프롬프트 생성
     */
    public String generatePricePrompt(PriceRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 중고거래 플랫폼의 AI 가격 전문가입니다.\n");
        prompt.append("아래 정보를 바탕으로 사용자가 판매하려는 상품의 적정가를 친근하고 쉽게 추천해주세요.\n");
        prompt.append("SQL, 기술적 용어, 코드, 표는 포함하지 말고, 핵심 정보와 근거만 간단하게 설명해주세요.\n\n");

        prompt.append("1. 사용자 요청: ").append(request.getUserRequest()).append("\n");
        prompt.append("- 카테고리: ").append(request.getCategory()).append("\n");
        prompt.append("- 상태: ").append(request.getCondition()).append("\n");
        if (request.getCurrentPrice() != null) {
            prompt.append("- 현재 가격: ").append(request.getCurrentPrice()).append("원\n");
        }
        prompt.append("\n");

        prompt.append("위 정보를 바탕으로 다음을 답변해주세요:\n");
        prompt.append("1. 추천 가격 범위 (최소, 최대, 평균)\n");
        prompt.append("2. 가격 결정의 주요 근거와 영향 요소\n");
        prompt.append("3. 시장 상황에 따른 가격 전략\n");
        if (request.getCurrentPrice() != null) {
            prompt.append("4. 현재 가격과의 비교 및 조정 제안\n");
        }
        prompt.append("답변은 친근하고 간단하게, 숫자는 한글 없이 숫자만 표기해주세요.\n");
        prompt.append("예시: '이 상품의 적정가는 7만원~9만원입니다! 최근 거래와 상태를 참고했어요.'");

        return prompt.toString();
    }

    /**
     * [가격 추천] (옵션) 가격 결정 요소까지 포함한 프롬프트 생성
     */
    public String generatePriceRecommendationPrompt(PriceRecommendationRequest request, List<PriceFactor> factors) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 중고거래 플랫폼의 AI 가격 전문가입니다.\n");
        prompt.append("아래 정보와 데이터 분석을 바탕으로 사용자가 판매하려는 상품의 적정가를 친근하고 쉽게 추천해주세요.\n");
        prompt.append("SQL, 기술적 용어, 코드, 표는 포함하지 말고, 핵심 정보와 근거만 간단하게 설명해주세요.\n\n");

        // 사용자 요청 정보
        prompt.append("1. 사용자 요청: ").append(request.getUserRequest()).append("\n");
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
        prompt.append("위 데이터를 바탕으로 다음 정보를 제공해주세요:\n\n");
        prompt.append("1. 추천 가격 범위 (최소, 최대, 평균)\n");
        prompt.append("2. 가격 결정의 주요 근거와 영향 요소\n");
        prompt.append("3. 시장 상황에 따른 가격 전략\n");
        if (request.getCurrentPrice() != null) {
            prompt.append("4. 현재 가격과의 비교 및 조정 제안\n");
        }
        prompt.append("답변은 친근하고 간단하게, 숫자는 한글 없이 숫자만 표기해주세요.\n");
        prompt.append("예시: '이 상품의 적정가는 7만원~9만원입니다! 최근 거래와 상태를 참고했어요.'");

        return prompt.toString();
    }
}
