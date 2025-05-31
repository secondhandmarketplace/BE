package ac.su.kdt.secondhandmarketplace.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 가격 결정 요소를 나타내는 DTO
 * 내부 플랫폼 데이터와 외부 중고거래 사이트 데이터를 모두 고려하여 가격을 결정합니다.
 */
@Data
public class PriceFactor {
    /**
     * 데이터 소스 구분
     * INTERNAL: 내부 플랫폼 데이터
     * EXTERNAL: 외부 중고거래 사이트 데이터
     */
    private String dataSource;

    /**
     * 가격 결정 요소의 이름
     * 예: "내부_동일제품_평균가", "외부_중고나라_평균가", "내부_상품상태_분석" 등
     */
    private String factorName;

    /**
     * 해당 요소가 가격에 미치는 영향에 대한 상세 설명
     * 예: "내부 플랫폼의 동일 제품 평균가 50만원 기준"
     *     "외부 사이트의 유사 상태 제품 평균가 45만원 기준"
     */
    private String description;

    /**
     * 해당 요소가 가격에 미치는 영향도
     * 양수: 가격 상승 요인
     * 음수: 가격 하락 요인
     * 예: 0.1 (10% 상승), -0.05 (5% 하락)
     */
    private BigDecimal impact;

    /**
     * 참고한 데이터의 수
     * 예: 내부 플랫폼의 동일 제품 수, 외부 사이트의 유사 제품 수
     */
    private Integer referenceCount;

    /**
     * 참고한 데이터의 평균 가격
     */
    private BigDecimal referenceAveragePrice;

    /**
     * 참고한 데이터의 최소 가격
     */
    private BigDecimal referenceMinPrice;

    /**
     * 참고한 데이터의 최대 가격
     */
    private BigDecimal referenceMaxPrice;
} 