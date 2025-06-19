package ac.su.kdt.secondhandmarketplace.service;

import ac.su.kdt.secondhandmarketplace.dto.transaction.TransactionCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.transaction.TransactionResponse;
import ac.su.kdt.secondhandmarketplace.entity.Product;
import ac.su.kdt.secondhandmarketplace.entity.ProductStatus;
import ac.su.kdt.secondhandmarketplace.entity.Transaction;
import ac.su.kdt.secondhandmarketplace.entity.User;
import ac.su.kdt.secondhandmarketplace.repository.ProductRepository;
import ac.su.kdt.secondhandmarketplace.repository.TransactionRepository;
import ac.su.kdt.secondhandmarketplace.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    //새로운 거래를 생성 (상품 판매 완료 처리 포함)
    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        // 1. 상품 존재 여부 및 상태 확인
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + request.getProductId()));

        // 상품이 판매중인 상태인지 확인 (예약중이거나 이미 판매완료된 상품은 거래 불가)
        if (product.getStatus() != ProductStatus.FOR_SALE) {
            throw new IllegalStateException("해당 상품은 판매중 상태가 아니므로 거래를 완료할 수 없습니다. 현재 상태: " + product.getStatus().getKoreanStatus());
        }

        // 2. 구매자 존재 여부 확인
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("구매자를 찾을 수 없습니다: " + request.getBuyerId()));

        // 3. 판매자와 구매자가 동일인인지 확인 (스스로 구매 방지)
        if (product.getUser().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("판매자는 자신의 상품을 구매할 수 없습니다.");
        }

        // 4. 거래 엔티티 생성 및 초기화
        Transaction transaction = new Transaction();
        transaction.setProduct(product); // 관련 상품 설정
        transaction.setBuyer(buyer);     // 구매자 설정
        transaction.setFinalPrice(request.getFinalPrice()); // 최종 거래 가격 설정
        transaction.setTransactionDate(LocalDateTime.now()); // 거래 완료 시간 설정

        // 5. 상품 상태를 '판매완료'로 변경
        product.setStatus(ProductStatus.SOLD_OUT); // 상품 상태 SOLD_OUT으로 변경
        product.setSoldAt(LocalDateTime.now()); // 판매 완료 시간 기록
        productRepository.save(product); // 변경된 상품 정보 저장

        // 6. 거래를 데이터베이스에 저장
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 7. 저장된 거래 엔티티를 응답 DTO로 변환하여 반환
        return TransactionResponse.fromEntity(savedTransaction);
    }

    // 특정 ID의 거래 내역을 조회
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정 (성능 최적화)
    public TransactionResponse getTransactionById(Long transactionId) {
        // 거래 ID로 Transaction 엔티티를 찾습니다. 없으면 예외 발생
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("거래 내역을 찾을 수 없습니다: " + transactionId));
        
        // 조회된 거래 엔티티를 응답 DTO로 변환하여 반환
        return TransactionResponse.fromEntity(transaction);
    }

    // 모든 거래 내역을 페이징하여 조회
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        // 모든 거래 내역을 페이징하여 조회하고, 각 Transaction 엔티티를 TransactionResponse DTO로 변환하여 반환
        return transactionRepository.findAll(pageable)
                .map(TransactionResponse::fromEntity); // Stream API의 map을 사용하여 각 엔티티를 DTO로 변환
    }

    // 특정 구매자의 거래 내역을 조회
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByBuyer(Long buyerId, Pageable pageable) {
        // 구매자 ID로 거래 내역을 페이징하여 조회하고, TransactionResponse DTO로 변환
        return transactionRepository.findByBuyer_Id(buyerId, pageable)
                .map(TransactionResponse::fromEntity);
    }

    // 특정 상품의 거래 내역을 조회
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByProduct(Long productId, Pageable pageable) {
        // 상품 ID로 거래 내역을 페이징하여 조회하고, TransactionResponse DTO로 변환
        return transactionRepository.findByProduct_Id(productId, pageable)
                .map(TransactionResponse::fromEntity);
    }
}