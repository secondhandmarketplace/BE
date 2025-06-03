package com.kdt.backend.controller;

import com.kdt.backend.dto.ItemRegisterRequestDTO;
import com.kdt.backend.dto.ItemResponseDTO;
import com.kdt.backend.dto.ItemSuggestionDTO;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.Item;
import com.kdt.backend.repository.ChatRoomRepository;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    // ✅ 이미지 서빙 엔드포인트 추가
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // 절대 경로로 파일 찾기
            Path filePath = Paths.get(System.getProperty("user.dir"), uploadPath, filename);
            Resource resource = new UrlResource(filePath.toUri());

            System.out.println("=== 이미지 요청 ===");
            System.out.println("요청 파일: " + filename);
            System.out.println("파일 경로: " + filePath.toAbsolutePath());
            System.out.println("파일 존재: " + Files.exists(filePath));
            System.out.println("파일 읽기 가능: " + Files.isReadable(filePath));

            if (resource.exists() && resource.isReadable()) {
                String contentType = getContentType(filename);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                        .body(resource);
            } else {
                System.err.println("파일을 찾을 수 없거나 읽을 수 없음: " + filePath);

                // 업로드 디렉토리의 모든 파일 목록 출력 (디버깅용)
                try {
                    Path uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
                    if (Files.exists(uploadDir)) {
                        System.out.println("업로드 디렉토리 파일 목록:");
                        Files.list(uploadDir).forEach(file ->
                                System.out.println("  - " + file.getFileName()));
                    } else {
                        System.err.println("업로드 디렉토리가 존재하지 않음: " + uploadDir);
                    }
                } catch (Exception e) {
                    System.err.println("디렉토리 목록 조회 실패: " + e.getMessage());
                }

                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("이미지 서빙 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ 이미지 업로드 엔드포인트
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {
            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일이 비어있습니다."));
            }

            // 파일 크기 검증 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일 크기는 5MB 이하여야 합니다."));
            }

            // 파일 확장자 검증
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isImageFile(originalFilename)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("업로드 디렉토리 생성: " + uploadDir);
            }

            // 고유한 파일명 생성
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadDir.resolve(uniqueFilename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("파일 저장 완료: " + filePath);

            // 응답 데이터
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", "/uploads/" + uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("message", "업로드 성공");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("파일 업로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // ✅ JSON 형태로 상품 등록
    @PostMapping(value = "/items",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerItem(
            @RequestBody ItemRegisterRequestDTO requestDTO) {
        try {
            Long itemId = itemService.saveItem(requestDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("itemid", itemId);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("상품 등록 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "상품 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // ✅ MultipartFile로 상품 등록
    @PostMapping(value = "/items/multipart",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> registerItemWithFiles(
            @RequestPart("item") ItemRegisterRequestDTO requestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            Long itemId = itemService.saveItemWithImages(requestDTO, images);

            Map<String, Object> response = new HashMap<>();
            response.put("itemid", itemId);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "상품 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponseDTO> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemResponseById(id));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        try {
            List<ItemResponseDTO> items = itemService.getAllItems();
            return ResponseEntity.ok(items != null ? items : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("아이템 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 기존 메서드들...
    @GetMapping("/items/mine")
    public ResponseEntity<List<ItemResponseDTO>> getMyItems(@RequestParam String userId) {
        return ResponseEntity.ok(itemService.getItemsBySellerId(userId));
    }

    @PutMapping(value = "/items/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateItem(
            @PathVariable Long id,
            @RequestPart("item") ItemRegisterRequestDTO requestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        itemService.updateItem(id, requestDTO, images);
        Map<String, Object> response = new HashMap<>();
        response.put("itemid", id);
        response.put("message", "게시글이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/items/{id}/status")
    public ResponseEntity<Map<String, String>> updateItemStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        itemService.updateItemStatus(id, status);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글 상태가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{itemId}/complete")
    public ResponseEntity<?> completeItemDeal(
            @PathVariable Long itemId,
            @RequestParam Long chatRoomId) {
        System.out.println("📩 거래 완료 요청: itemId=" + itemId + ", chatRoomId=" + chatRoomId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 없습니다."));

        item.setBuyer(chatRoom.getBuyer());
        item.setStatus(Item.Status.거래완료);
        item.setCompletedDate(LocalDateTime.now());

        itemRepository.save(item);

        return ResponseEntity.ok("거래 완료 처리되었습니다.");
    }

    @GetMapping("/items/completed")
    public ResponseEntity<List<ItemResponseDTO>> getCompletedItemsByBuyer(@RequestParam String userId) {
        return ResponseEntity.ok(itemService.getCompletedItemsByBuyer(userId));
    }

    @GetMapping("/items/suggest")
    public ResponseEntity<List<ItemSuggestionDTO>> getSuggestions(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.getItemSuggestionsWithImage(keyword));
    }

    @GetMapping("/items/by-seller")
    public ResponseEntity<List<ItemResponseDTO>> getItemsBySeller(@RequestParam String sellerId) {
        return ResponseEntity.ok(itemService.getItemsBySeller(sellerId));
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<ItemResponseDTO>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }

    @GetMapping("/items/category")
    public ResponseEntity<List<ItemResponseDTO>> getItemsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(itemService.getItemsByCategory(category));
    }

    @GetMapping("/items/related")
    public ResponseEntity<List<ItemResponseDTO>> getRelatedItems(
            @RequestParam String category,
            @RequestParam Long excludeId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(itemService.getRelatedItems(category, excludeId, limit));
    }

    @PostMapping("/items/like")
    public ResponseEntity<Map<String, Object>> likeItem(@RequestBody Map<String, Object> request) {
        Long itemId = Long.valueOf(request.get("itemId").toString());
        String userId = request.get("userId").toString();

        boolean success = itemService.toggleLike(itemId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "찜 목록에 추가되었습니다." : "이미 찜한 상품입니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{id}/view")
    public ResponseEntity<Map<String, String>> incrementViewCount(@PathVariable Long id) {
        itemService.incrementViewCount(id);
        return ResponseEntity.ok(Map.of("message", "조회수가 증가되었습니다."));
    }

    // 헬퍼 메서드들
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    private String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case ".png": return "image/png";
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".gif": return "image/gif";
            case ".webp": return "image/webp";
            case ".bmp": return "image/bmp";
            default: return "application/octet-stream";
        }
    }
}
