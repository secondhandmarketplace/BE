package ac.su.kdt.secondhandmarketplace.dto.transaction;

import ac.su.kdt.secondhandmarketplace.entity.Transaction;
import ac.su.kdt.secondhandmarketplace.dto.product.ProductResponse;
// import ac.su.kdt.secondhandmarketplace.dto.user.UserResponse; // UserResponse DTO 임포트 (UserResponse는 추후 구현)
import ac.su.kdt.secondhandmarketplace.dto.transaction.ReviewResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId; // 거래 고유 식별자
    private Long productId;     // 관련 상품 ID
    private String productTitle; // 관련 상품명
    private Long buyerId;       // 구매자 ID
    private String buyerUsername; // 구매자 닉네임 (또는 사용자명)
    private BigDecimal finalPrice; // 최종 거래 가격
    private LocalDateTime transactionDate; // 거래 완료 시간
    private ReviewResponse review; // 관련 리뷰 정보 (ReviewResponse DTO)

    // Transaction 엔티티를 TransactionResponse DTO로 변환하는 정적 팩토리 메서드
    public static TransactionResponse fromEntity(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getId()); // 거래 ID 설정
        response.setProductId(transaction.getProduct() != null ? transaction.getProduct().getId() : null); // 상품 ID 설정
        response.setProductTitle(transaction.getProduct() != null ? transaction.getProduct().getTitle() : null); // 상품명 설정
        response.setBuyerId(transaction.getBuyer() != null ? transaction.getBuyer().getId() : null); // 구매자 ID 설정
        response.setBuyerUsername(transaction.getBuyer() != null ? transaction.getBuyer().getUsername() : null); // 구매자 이름 설정
        response.setFinalPrice(transaction.getFinalPrice()); // 최종 가격 설정
        response.setTransactionDate(transaction.getTransactionDate()); // 거래 일시 설정
        // 리뷰가 존재하면 ReviewResponse DTO로 변환하여 설정
        response.setReview(transaction.getReview() != null ? ReviewResponse.fromEntity(transaction.getReview()) : null);
        return response; // 변환된 TransactionResponse DTO 반환
    }
}