package com.kdt.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Slf4j
public class ImageController {

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    /**
     * ✅ 이미지 업로드 (단일)
     */
    @PostMapping("/api/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("이미지 업로드 요청: filename={}, size={}", file.getOriginalFilename(), file.getSize());
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "파일이 비어있습니다."
                ));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "이미지 파일만 업로드 가능합니다."
                ));
            }
            Path uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지 업로드 성공");
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("imageUrl", "/api/image/" + uniqueFilename); // 프론트에서 이 경로로 접근
            response.put("size", file.getSize());
            response.put("contentType", contentType);
            response.put("timestamp", LocalDateTime.now());

            log.info("이미지 업로드 완료: {}", uniqueFilename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "파일 저장 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ 이미지 서빙 (업로드 경로)
     */
    @GetMapping("/api/upload/{filename:.+}")
    public ResponseEntity<Resource> getUploadedImage(@PathVariable String filename) {
        return getImageResource(filename);
    }

    /**
     * ✅ 이미지 서빙 (채팅용 경로)
     */
    @GetMapping("/api/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return getImageResource(filename);
    }

    /**
     * ✅ 이미지 서빙 (uploads 폴더 직접 접근)
     */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getUploadsImage(@PathVariable String filename) {
        return getImageResource(filename);
    }

    /**
     * ✅ 공통 이미지 리소스 처리
     */
    private ResponseEntity<Resource> getImageResource(String filename) {
        try {
            log.info("이미지 요청: {}", filename);

            // ✅ 파일명 정리 (경로 구분자 제거)
            String cleanFilename = filename.replaceAll("[/\\\\]", "");

            Path filePath = Paths.get(System.getProperty("user.dir"), uploadPath, cleanFilename);
            Resource resource = new UrlResource(filePath.toUri());

            log.info("이미지 파일 경로: {}", filePath.toString());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(cleanFilename);
                log.info("이미지 서빙 성공: {} -> {}", filename, cleanFilename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .body(resource);
            } else {
                log.warn("이미지 파일을 찾을 수 없음: {} (경로: {})", filename, filePath.toString());

                // ✅ 이미지가 없을 때: HTML 반환이 아니라 빈 PNG로 응답!
                InputStream emptyPng = getClass().getResourceAsStream("/static/empty.png");
                if (emptyPng != null) {
                    return ResponseEntity.status(404)
                            .contentType(MediaType.IMAGE_PNG)
                            .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                            .body(new InputStreamResource(emptyPng));
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            log.error("이미지 서빙 실패: filename={}, error={}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : ".jpg";
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

}
