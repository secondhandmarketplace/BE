package com.kdt.backend.controller;

import com.kdt.backend.dto.FavoriteItemDTO;
import com.kdt.backend.entity.Item;
import com.kdt.backend.service.ItemLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class ItemLikeController {

    private final ItemLikeService itemLikeService;

    @PostMapping("/{itemId}")
    public ResponseEntity<Void> like(@PathVariable Long itemId, @RequestParam String userId) {
        itemLikeService.likeItem(itemId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> unlike(@PathVariable Long itemId, @RequestParam String userId) {
        itemLikeService.unlikeItem(itemId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{itemId}/count")
    public ResponseEntity<Long> count(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemLikeService.countLikes(itemId));
    }

    @GetMapping("/{itemId}/is-liked")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long itemId, @RequestParam String userId) {
        return ResponseEntity.ok(itemLikeService.isLiked(itemId, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<FavoriteItemDTO>> myFavorites(@RequestParam String userId) {
        return ResponseEntity.ok(itemLikeService.getUserFavorites(userId));
    }

}
