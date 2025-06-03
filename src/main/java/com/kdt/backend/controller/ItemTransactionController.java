package com.kdt.backend.controller;

import com.kdt.backend.repository.ItemTransactionRepository;
import com.kdt.backend.service.ItemTransactionService;
import com.kdt.backend.dto.TransactionRequestDTO;
import com.kdt.backend.entity.ItemTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class ItemTransactionController {

    private final ItemTransactionService itemTransactionService;
    private final ItemTransactionRepository itemTransactionRepository;

    @PostMapping
    public ResponseEntity<ItemTransaction> createTransaction(@RequestBody TransactionRequestDTO dto) {
        ItemTransaction transaction = itemTransactionService.createTransaction(
                dto.getBuyerId(), dto.getSellerId(), dto.getItemId());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/item/{itemId}/user/{userId}")
    public ResponseEntity<Map<String, Long>> getTransactionId(
            @PathVariable Long itemId,
            @PathVariable String userId) {

        List<ItemTransaction> transactions = itemTransactionRepository
                .findAllByItem_ItemidAndUser_UseridAndDistinctSellerNot(itemId, userId, userId);

        if (transactions.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", -1L));
        }

        // ✅ 여러 개 중 첫 번째만 사용
        Long transactionId = transactions.get(0).getTransactionid();
        return ResponseEntity.ok(Map.of("transactionId", transactionId));
    }
}
