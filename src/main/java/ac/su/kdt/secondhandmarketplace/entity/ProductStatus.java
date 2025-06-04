package ac.su.kdt.secondhandmarketplace.entity;

import lombok.Getter;

// 상품의 상태를 나타내는 열거형 (Enum)입니다.
@Getter
public enum ProductStatus {
    // Enum 상수는 Java 명명 규칙에 따라 대문자 SNAKE_CASE를 사용
    // 각 상수에 데이터베이스에 저장될 실제 값을 부여
    FOR_SALE("판매중"),       // 판매중
    RESERVED("예약중"),      // 예약중
    SOLD_OUT("판매완료"),    // 판매완료
    DELETED("삭제됨");       // 삭제됨

    // 한글 값을 반환하는 getter 메서드
    private final String koreanStatus; // 데이터베이스에 저장될 한글 값을 저장할 필드

    // 생성자: Enum 상수가 생성될 때 한글 값을 초기화
    ProductStatus(String koreanStatus) {
        this.koreanStatus = koreanStatus;
    }

    // 데이터베이스에서 읽어온 한글 문자열로부터 Enum 상수를 찾아 반환하는 정적 메서드
    public static ProductStatus fromKoreanStatus(String koreanStatus) {
        // 모든 ProductStatus Enum 상수를 반복하면서
        for (ProductStatus status : ProductStatus.values()) {
            // 현재 Enum 상수의 한글 값과 입력된 한글 값이 일치하면 해당 Enum 상수를 반환
            if (status.koreanStatus.equals(koreanStatus)) {
                return status;
            }
        }
        // 일치하는 한글 값이 없으면 예외를 발생시켜 잘못된 값을 처리
        throw new IllegalArgumentException("알 수 없는 상품 상태: " + koreanStatus);
    }
}