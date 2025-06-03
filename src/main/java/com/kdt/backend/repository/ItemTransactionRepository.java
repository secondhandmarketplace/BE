package com.kdt.backend.repository;

import com.kdt.backend.entity.ItemTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemTransactionRepository extends JpaRepository<ItemTransaction, Long> {

    // 후기 작성용: 구매자가 작성자고 판매자가 아닌 경우
    List<ItemTransaction> findAllByItem_ItemidAndUser_UseridAndDistinctSellerNot(Long itemId, String userId, String distinctSeller);

    // 선택적으로 사용할 수도 있는 단건 조회 (중복 시 오류 발생하므로 사용 주의)
    Optional<ItemTransaction> findByItem_ItemidAndUser_UseridAndDistinctSellerNot(Long itemId, String buyerId, String buyerIdAgain);

    // 삭제 기능용: 해당 item과 연결된 모든 거래 내역
    List<ItemTransaction> findAllByItem_Itemid(Long itemId);
}
