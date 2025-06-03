    package com.kdt.backend.service;

    import com.kdt.backend.dto.ItemRegisterRequestDTO;
    import com.kdt.backend.dto.ItemResponseDTO;
    import com.kdt.backend.dto.ItemSuggestionDTO;
    import com.kdt.backend.entity.*;
    import com.kdt.backend.repository.*;
    import jakarta.persistence.EntityManager;
    import jakarta.persistence.PersistenceContext;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.math.BigDecimal;
    import java.nio.file.*;
    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.time.ZoneId;
    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class ItemService {

        private final ChatMessageRepository chatMessageRepository;
        private final ItemRepository itemRepository;
        private final ItemImageRepository itemImageRepository;
        private final UserRepository userRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final ItemTransactionRepository itemTransactionRepository;
        private final ReviewRepository reviewRepository;

        @PersistenceContext
        private EntityManager em;

        public List<ItemResponseDTO> searchItems(String keyword) {
            List<Item> items = itemRepository.findTop10ByKeyword(keyword);
            return items.stream()
                    .map(item -> ItemResponseDTO.from(item, reviewRepository))
                    .collect(Collectors.toList());
        }

        // ✅ 카테고리별 조회
        public List<ItemResponseDTO> getItemsByCategory(String category) {
            List<Item> items = itemRepository.findByCategoryAndStatus(category, Item.Status.valueOf("판매중"));
            return items.stream()
                    .map(item -> ItemResponseDTO.from(item, reviewRepository))
                    .collect(Collectors.toList());
        }

        // ✅ 연관 상품 조회
        public List<ItemResponseDTO> getRelatedItems(String category, Long excludeId, int limit) {
            List<Item> items = itemRepository.findByCategoryAndItemidNotAndStatusOrderByRegDateDesc(
                    category, excludeId, Item.Status.판매중);
            return items.stream()
                    .limit(limit)
                    .map(item -> ItemResponseDTO.from(item, reviewRepository))
                    .collect(Collectors.toList());
        }

        // ✅ 찜하기 토글
        public boolean toggleLike(Long itemId, String userId) {
            // 실제 구현은 Like 엔티티가 있다면 그에 맞게 구현
            // 여기서는 간단한 예시
            try {
                // 찜 목록 확인 및 추가/제거 로직
                return true; // 성공
            } catch (Exception e) {
                return false; // 실패 또는 이미 존재
            }
        }

        // ✅ 조회수 증가
        public void incrementViewCount(Long itemId) {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            item.incrementViewCount();
            itemRepository.save(item);
        }

        // ✅ 판매자별 조회 (기존에 없다면)
        public List<ItemResponseDTO> getItemsBySeller(String sellerId) {
            List<Item> items = itemRepository.findBySellerUserIdWithImages(sellerId);
            return items.stream()
                    .map(item -> ItemResponseDTO.from(item, reviewRepository))
                    .collect(Collectors.toList());
        }


        public Item getItemById(Long id) {
            return itemRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다."));
        }

        public ItemResponseDTO getItemResponseById(Long id) {
            Item item = itemRepository.findItemWithSellerAndImagesById(id)
                    .orElseThrow(() -> new NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다."));

            // 디버깅 로그 추가
            System.out.println("=== ItemService에서 Item 조회:");
            System.out.println("itemId: " + item.getItemid());
            System.out.println("title: " + item.getTitle());
            System.out.println("meetLocation: " + item.getMeetLocation());
            System.out.println("이미지 개수: " + (item.getItemImages() != null ? item.getItemImages().size() : 0));

            ItemResponseDTO dto = toResponseDTO(item);

            // DTO 변환 후 확인
            System.out.println("=== DTO 변환 후:");
            System.out.println("meetLocation: " + dto.getMeetLocation());
            System.out.println("locationInfo: " + dto.getLocationInfo());
            System.out.println("itemImages: " + dto.getItemImages());

            return dto;
        }

        public List<ItemResponseDTO> getAllItems() {
            return itemRepository.findAllWithSellerAndImages().stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());
        }

        public List<ItemResponseDTO> getItemsBySellerId(String userId) {
            return itemRepository.findBySellerUserIdWithImages(userId).stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());
        }

        /**
         * Item 엔티티를 ItemResponseDTO로 변환 (meetLocation 및 이미지 포함)
         */
        public ItemResponseDTO toResponseDTO(Item item) {
            System.out.println("=== toResponseDTO 변환:");
            System.out.println("item.getItemImages(): " + item.getItemImages());

            List<String> imagePaths = new ArrayList<>();
            if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                imagePaths = item.getItemImages().stream()
                        .map(ItemImage::getPhotoPath)
                        .collect(Collectors.toList());
                System.out.println("=== 이미지 경로들: " + imagePaths);
            }

            return ItemResponseDTO.builder()
                    .id(item.getItemid())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .price(BigDecimal.valueOf(item.getPrice()))
                    .category(item.getCategory())
                    .meetLocation(item.getMeetLocation()) // meetLocation 명시적 설정
                    .locationInfo(item.getMeetLocation()) // locationInfo도 동일하게 설정
                    .regdate(String.valueOf(item.getRegDate()))
                    .sellerId(item.getSeller() != null ? item.getSeller().getUserid() : null)
                    .sellerName(item.getSeller() != null ? item.getSeller().getName() : null)
                    .status(item.getStatus().name())
                    .viewCount(item.getViewCount())
                    .thumbnail(item.getThumbnail())
                    .itemImages(imagePaths) // 실제 이미지 경로 리스트
                    .build();
        }

        // ItemService.java에 추가
        public Long saveItem(ItemRegisterRequestDTO requestDTO) {
            User seller = userRepository.findById(requestDTO.getSellerId())
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            System.out.println("=== JSON 상품 저장 요청:");
            System.out.println("title: " + requestDTO.getTitle());
            System.out.println("meetLocation: " + requestDTO.getMeetLocation());

            Item item = Item.builder()
                    .seller(seller)
                    .title(requestDTO.getTitle())
                    .description(requestDTO.getDescription())
                    .price(requestDTO.getPrice())
                    .category(requestDTO.getCategory())
                    .status(Item.Status.판매중)
                    .meetLocation(requestDTO.getMeetLocation())
                    .regDate(LocalDateTime.now())
                    .viewCount(0)
                    .build();

            // 이미지 URL들이 있다면 ItemImage로 저장
            if (requestDTO.getItemImages() != null && !requestDTO.getItemImages().isEmpty()) {
                for (String imageUrl : requestDTO.getItemImages()) {
                    ItemImage image = ItemImage.builder()
                            .photoPath(imageUrl)
                            .regdate(LocalDateTime.now())
                            .build();
                    item.addItemImage(image);
                }
            }

            Item savedItem = itemRepository.save(item);
            return savedItem.getItemid();
        }


        /**
         * 이미지와 함께 상품 저장
         */
        public Long saveItemWithImages(ItemRegisterRequestDTO requestDTO, List<MultipartFile> images) {
            User seller = userRepository.findById(requestDTO.getSellerId())
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            // 디버깅 로그
            System.out.println("=== 상품 저장 요청:");
            System.out.println("title: " + requestDTO.getTitle());
            System.out.println("meetLocation: " + requestDTO.getMeetLocation());
            System.out.println("이미지 개수: " + (images != null ? images.size() : 0));

            Item item = Item.builder()
                    .seller(seller)
                    .title(requestDTO.getTitle())
                    .description(requestDTO.getDescription())
                    .price(requestDTO.getPrice())
                    .category(requestDTO.getCategory())
                    .status(Item.Status.판매중)
                    .meetLocation(requestDTO.getMeetLocation()) // meetLocation 설정
                    .regDate(LocalDateTime.now())
                    .viewCount(0)
                    .build();

            // 이미지 처리
            if (images != null && !images.isEmpty()) {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                try {
                    Files.createDirectories(uploadPath);
                } catch (IOException e) {
                    throw new RuntimeException("디렉토리 생성 실패: " + e.getMessage());
                }

                for (MultipartFile file : images) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename == null || !originalFilename.contains(".")) {
                            continue; // 확장자가 없는 파일은 건너뛰기
                        }

                        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String saveFileName = UUID.randomUUID().toString().replace("-", "") + ext;
                        Path filePath = uploadPath.resolve(saveFileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        ItemImage image = ItemImage.builder()
                                .photoPath("/uploads/" + saveFileName)
                                .regdate(LocalDateTime.now())
                                .build();
                        item.addItemImage(image);

                        System.out.println("=== 이미지 저장됨: /uploads/" + saveFileName);
                    } catch (IOException e) {
                        System.err.println("=== 이미지 저장 실패: " + e.getMessage());
                        throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
                    }
                }
            }

            Item savedItem = itemRepository.save(item);
            System.out.println("=== 저장된 Item meetLocation: " + savedItem.getMeetLocation());
            System.out.println("=== 저장된 Item 이미지 개수: " + savedItem.getItemImages().size());

            return savedItem.getItemid();
        }

        /**
         * 상품 정보 수정
         */
        public void updateItem(Long itemId, ItemRegisterRequestDTO requestDTO, List<MultipartFile> images) {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다."));

            // 디버깅 로그
            System.out.println("=== 상품 수정 요청:");
            System.out.println("기존 meetLocation: " + item.getMeetLocation());
            System.out.println("새로운 meetLocation: " + requestDTO.getMeetLocation());

            item.setTitle(requestDTO.getTitle());
            item.setDescription(requestDTO.getDescription());
            item.setPrice(requestDTO.getPrice());
            item.setCategory(requestDTO.getCategory());
            item.setMeetLocation(requestDTO.getMeetLocation()); // meetLocation 업데이트

            // 기존 이미지 삭제
            if (item.getItemImages() != null) {
                item.getItemImages().clear();
            }

            // 새 이미지 추가
            if (images != null && !images.isEmpty()) {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                try {
                    Files.createDirectories(uploadPath);
                } catch (IOException e) {
                    throw new RuntimeException("디렉토리 생성 실패: " + e.getMessage());
                }

                for (MultipartFile file : images) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename == null || !originalFilename.contains(".")) {
                            continue;
                        }

                        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String saveFileName = UUID.randomUUID().toString().replace("-", "") + ext;
                        Path filePath = uploadPath.resolve(saveFileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        ItemImage image = ItemImage.builder()
                                .photoPath("/uploads/" + saveFileName)
                                .regdate(LocalDateTime.now())
                                .build();
                        item.addItemImage(image);
                    } catch (IOException e) {
                        throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
                    }
                }
            }

            System.out.println("=== 수정 완료 후 meetLocation: " + item.getMeetLocation());
        }

        /**
         * 상품 삭제 (관련 데이터 모두 삭제)
         */
        public void deleteItem(Long itemId) {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다."));

            // 거래 내역 조회
            List<ItemTransaction> transactions = itemTransactionRepository.findAllByItem_Itemid(itemId);

            for (ItemTransaction transaction : transactions) {
                // 채팅방 조회 및 메시지 삭제
                List<ChatRoom> chatRooms = chatRoomRepository.findAllByItemTransaction_Transactionid(transaction.getTransactionid());
                for (ChatRoom chatRoom : chatRooms) {
                    chatMessageRepository.deleteAllByChatRoom_Chatroomid(chatRoom.getChatroomid());
                }
                chatRoomRepository.deleteAll(chatRooms);
            }

            itemTransactionRepository.deleteAll(transactions);

            // 이미지도 함께 삭제
            if (item.getItemImages() != null) {
                itemImageRepository.deleteAll(item.getItemImages());
            }

            // 최종 게시글 삭제
            itemRepository.delete(item);
        }

        /**
         * 상품 상태 변경
         */
        public void updateItemStatus(Long itemId, String status) {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다."));

            try {
                Item.Status newStatus = Item.Status.valueOf(status);
                item.setStatus(newStatus);

                // 거래완료 시 완료일 설정
                if (newStatus == Item.Status.거래완료) {
                    item.setCompletedDate(LocalDateTime.now());
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 상태 값입니다: " + status);
            }
        }

        /**
         * 구매자 기준 거래완료 글 목록
         */
        public List<ItemResponseDTO> getCompletedItemsByBuyer(String buyerId) {
            return itemRepository.findCompletedByBuyerUserId(buyerId).stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());
        }

        /**
         * 연관 키워드 추천 기능
         */
        @Transactional(readOnly = true)
        public List<ItemSuggestionDTO> getItemSuggestionsWithImage(String keyword) {
            return itemRepository.findTop10ByKeyword(keyword).stream()
                    .map(item -> new ItemSuggestionDTO(
                            item.getItemid(),
                            item.getTitle(),
                            item.getItemImages() != null && !item.getItemImages().isEmpty()
                                    ? item.getItemImages().get(0).getPhotoPath()
                                    : null
                    ))
                    .collect(Collectors.toList());
        }

        /**
         * 판매자별 상품 목록 조회
         */

        /**
         * 사용자 요청에 따른 스마트 필터링된 상품 목록 반환
         */
        public List<Item> getFilteredItemsByQuery(String query) {
            List<Item> allItems = itemRepository.findAllWithSellerAndImages();

            System.out.println("=== 필터링 요청: " + query);
            System.out.println("=== 전체 상품 수: " + allItems.size());

            List<Item> filteredItems;

            if (query.contains("싼") || query.contains("저렴") || query.contains("최저가")) {
                filteredItems = allItems.stream()
                        .sorted((a, b) -> Integer.compare(a.getPrice(), b.getPrice()))
                        .limit(3)
                        .collect(Collectors.toList());
                System.out.println("=== 저가순 정렬 적용");
            } else if (query.contains("비싼") || query.contains("높은") || query.contains("최고가")) {
                filteredItems = allItems.stream()
                        .sorted((a, b) -> Integer.compare(b.getPrice(), a.getPrice()))
                        .limit(3)
                        .collect(Collectors.toList());
                System.out.println("=== 고가순 정렬 적용");
            } else if (query.contains("노트북")) {
                filteredItems = allItems.stream()
                        .filter(item -> item.getTitle().toLowerCase().contains("노트북"))
                        .limit(3)
                        .collect(Collectors.toList());
                System.out.println("=== 노트북 필터 적용");
            } else if (query.contains("청소기")) {
                filteredItems = allItems.stream()
                        .filter(item -> item.getTitle().toLowerCase().contains("청소기"))
                        .limit(3)
                        .collect(Collectors.toList());
                System.out.println("=== 청소기 필터 적용");
            } else {
                // 기본: 최신순
                filteredItems = allItems.stream()
                        .sorted((a, b) -> b.getRegDate().compareTo(a.getRegDate()))
                        .limit(3)
                        .collect(Collectors.toList());
                System.out.println("=== 최신순 정렬 적용");
            }

            System.out.println("=== 필터링된 상품 수: " + filteredItems.size());
            return filteredItems;
        }
    }
