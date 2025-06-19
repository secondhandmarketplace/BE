package ac.su.kdt.secondhandmarketplace.dto.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionCreateRequest {
    private Long productId; // 관련 상품 ID

    private Long buyerId; // 구매자 ID

    private BigDecimal finalPrice; // 최종 거래 가격
}