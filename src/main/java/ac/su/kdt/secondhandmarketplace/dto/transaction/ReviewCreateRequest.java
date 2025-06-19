package ac.su.kdt.secondhandmarketplace.dto.transaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {
    private Long transactionId; // 관련 거래 ID

    private Long reviewerId; // 리뷰 작성자 ID

    private Integer rating; // 평점 (1~5점)

    private String content; // 리뷰 내용 (선택 사항)
}