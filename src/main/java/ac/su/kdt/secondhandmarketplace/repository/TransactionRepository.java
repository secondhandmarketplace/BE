package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 특정 구매자(user_id)에 해당하는 거래내역을 페이징하여 조회
    Page<Transaction> findByBuyer_Id(Long Id, Pageable pageable);

    // 특정 상품(product_id)에 해당하는 거래내역을 페이징하여 조회
    Page<Transaction> findByProduct_Id(Long Id, Pageable pageable);

    // 특정 거래 ID에 해당하는 거래가 존재하는지 확인
    boolean existsById(Long Id);
}