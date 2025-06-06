package com.kdt.backend.service;

import com.kdt.backend.dto.ItemRegisterRequestDTO;
import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.ItemSuggestionDTO;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemImage;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    public Long saveItem(ItemRegisterRequestDTO request) {
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보가 없습니다."));

        Item item = Item.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(Item.Status.판매중)
                .seller(seller)
                .regDate(LocalDateTime.now())
                .meetLocation(request.getMeetLocation())
                .thumbnail(request.getThumbnail()) // ✅ 썸네일 저장
                .itemCondition(request.getCondition() != null ? request.getCondition() : request.getValue())
                .build();

        // 태그 저장
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            item.setTags(request.getTags());
        }

        // 이미지 저장 (OneToMany)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl == null || imageUrl.isBlank()) continue;
                String fileName = imageUrl;
                if (fileName.startsWith("/api/image/")) {
                    fileName = fileName.substring("/api/image/".length());
                }
                ItemImage itemImage = ItemImage.builder()
                        .photoPath(fileName)
                        .item(item)
                        .regdate(LocalDateTime.now())
                        .build();
                item.addItemImage(itemImage); // 반드시 addItemImage로 연결
            }
        }

        itemRepository.save(item);
        return item.getItemid();
    }


    /**
     * ✅ 정렬별 아이템 조회 (최근 등록순 [2] 반영)
     */
    public List<ItemResponseDTO> getItemsBySort(String sort, int size) {
        try {
            List<Item> items;

            switch (sort.toLowerCase()) {
                case "latest":
                default:
                    // ✅ 최근 등록순 정렬 (사용자 선호사항 [2])
                    items = itemRepository.findAllByOrderByRegDateDesc();
                    break;
                case "price_asc":
                    items = itemRepository.findByStatusOrderByPriceDesc(Item.Status.판매중);
                    break;
                case "popular":
                    items = itemRepository.findByStatusOrderByViewCountDesc(Item.Status.판매중);
                    break;
            }

            return items.stream()
                    .limit(size)
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("정렬별 아이템 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 단일 아이템 조회 (판매자 정보 포함)
     */
    public ItemResponseDTO getItemById(Long itemId) {
        try {
            Item item = itemRepository.findItemWithSellerAndImagesById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

            // ✅ 조회수 증가
            item.incrementViewCount();
            itemRepository.save(item);

            return ItemResponseDTO.from(item);

        } catch (Exception e) {
            log.error("아이템 조회 실패: itemId={}, error={}", itemId, e.getMessage());
            return null;
        }
    }

    /**
     * ✅ 단일 아이템 조회 (ResponseDTO 반환) - 별칭 메서드
     */
    public ItemResponseDTO getItemResponseById(Long id) {
        return getItemById(id);
    }

    /**
     * ✅ 전체 아이템 조회 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> getAllItems() {
        try {
            List<Item> items = itemRepository.findAllByOrderByRegDateDesc();
            return items.stream()
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("전체 아이템 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 판매자별 상품 조회 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> getItemsBySellerId(String sellerId) {
        try {
            List<Item> items = itemRepository.findBySellerUseridOrderByRegDateDesc(sellerId);
            return items.stream()
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("판매자별 상품 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 판매자별 상품 조회 (별칭 메서드)
     */
    public List<ItemResponseDTO> getItemsBySeller(String sellerId) {
        return getItemsBySellerId(sellerId);
    }

    /**
     * ✅ 검색 기능 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> searchItems(String keyword) {
        try {
            List<Item> items = itemRepository.findByKeywordOrderByRegDateDesc(keyword, Item.Status.판매중);
            return items.stream()
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("검색 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 카테고리별 조회 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> getItemsByCategory(String category) {
        try {
            List<Item> items = itemRepository.findByCategoryAndStatusOrderByRegDateDesc(
                    category, Item.Status.판매중);
            return items.stream()
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("카테고리별 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 상품 제안 조회 (대화형 인공지능 [5] 지원)
     */
    public List<ItemSuggestionDTO> getItemSuggestionsWithImage(String keyword) {
        try {
            List<Item> items = itemRepository.findByTitleContainingOrderByRegDateDesc(keyword);

            return items.stream()
                    .limit(10) // 최대 10개
                    .map(item -> ItemSuggestionDTO.builder()
                            .itemId(item.getItemid())
                            .title(item.getTitle())
                            .price(item.getPrice())
                            .thumbnail(item.getFirstImagePath())
                            .category(item.getCategory())
                            .sellerId(item.getSellerId())
                            .status(item.getStatus().name())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("상품 제안 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 상품 상태 변경
     */
    public void updateItemStatus(Long itemId, String status) {
        try {
            log.info("상품 상태 변경: ID={}, status={}", itemId, status);

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

            Item.Status newStatus = Item.Status.fromString(status);
            item.changeStatus(newStatus);

            itemRepository.save(item);
            log.info("상품 상태 변경 완료: ID={}, status={}", itemId, status);

        } catch (Exception e) {
            log.error("상품 상태 변경 실패: {}", e.getMessage());
            throw new RuntimeException("상품 상태 변경에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ✅ 조회수 증가
     */
    public void incrementViewCount(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        item.incrementViewCount();
        itemRepository.save(item);
    }

    // ===== 기타 메서드들 =====

    /**
     * ✅ 관련 상품 조회 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> getRelatedItems(String category, Long excludeId, int limit) {
        try {
            List<Item> items = itemRepository.findByCategoryAndItemidNotAndStatusOrderByRegDateDesc(
                    category, excludeId, Item.Status.판매중);
            return items.stream()
                    .limit(limit)
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("관련 상품 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * ✅ 완료된 거래 조회 (최근 등록순 [2])
     */
    public List<ItemResponseDTO> getCompletedItemsByBuyer(String userId) {
        try {
            List<Item> items = itemRepository.findCompletedByBuyerUserId(userId, Item.Status.거래완료);
            return items.stream()
                    .map(ItemResponseDTO::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("완료된 거래 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
