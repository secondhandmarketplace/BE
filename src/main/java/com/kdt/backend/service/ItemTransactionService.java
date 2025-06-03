package com.kdt.backend.service;

import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemTransaction;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.ItemTransactionRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemTransactionService {

    private final ItemTransactionRepository itemTransactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemTransaction createTransaction(String buyerId, String sellerId, Long itemId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자 정보가 없습니다."));
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("판매자 정보가 없습니다."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다."));

        ItemTransaction transaction = ItemTransaction.builder()
                .item(item)
                .user(buyer) // 구매자
                .distinctSeller(seller.getUserid()) // 판매자 ID 문자열
                .comment("")
                .build();

        return itemTransactionRepository.save(transaction);
    }
}
