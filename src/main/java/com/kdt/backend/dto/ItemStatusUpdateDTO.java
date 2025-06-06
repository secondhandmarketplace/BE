package com.kdt.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemStatusUpdateDTO {
    private String status; // "판매중", "예약중", "거래완료"
    private String reason; // 상태 변경 사유 (선택사항)
    private String buyerId; // 구매자 ID (거래완료 시)

    // 유효성 검증 메서드
    public boolean isValidStatus() {
        return status != null &&
                (status.equals("판매중") || status.equals("예약중") || status.equals("거래완료"));
    }
}
