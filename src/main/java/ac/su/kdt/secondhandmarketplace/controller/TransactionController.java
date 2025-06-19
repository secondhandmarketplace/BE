package ac.su.kdt.secondhandmarketplace.controller;

import ac.su.kdt.secondhandmarketplace.dto.transaction.TransactionCreateRequest;
import ac.su.kdt.secondhandmarketplace.dto.transaction.TransactionResponse;
import ac.su.kdt.secondhandmarketplace.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    //새로운 거래를 생성 (상품 판매 완료 처리 포함)
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionCreateRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    // 특정 ID의 거래 내역을 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    // 모든 거래 내역을 페이징하여 조회
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @PageableDefault(sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }
    // 특정 구매자의 거래 내역을 조회
    @GetMapping("/by-buyer/{buyerId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByBuyer(
            @PathVariable Long buyerId,
            @PageableDefault(sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByBuyer(buyerId, pageable); // Service 계층에 구매자별 거래 조회 요청
        return ResponseEntity.ok(transactions);
    }

    // 특정 상품의 거래 내역을 조회
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByProduct(
            @PathVariable Long productId,
            @PageableDefault(sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByProduct(productId, pageable); // Service 계층에 상품별 거래 조회 요청
        return ResponseEntity.ok(transactions);
    }
}