package com.kdt.backend.controller;

import com.kdt.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<Void> saveKeyword(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        String userId = body.get("userId");

        if (keyword == null || userId == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        searchService.addSearchHistory(userId, keyword.trim());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String userId, @RequestParam String keyword) {
        return ResponseEntity.ok(searchService.getSuggestions(userId, keyword));
    }

    @GetMapping
    public ResponseEntity<List<String>> getSearchHistory(@RequestParam String userId) {
        return ResponseEntity.ok(searchService.getSearchHistory(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestParam String userId) {
        searchService.deleteAllSearchHistory(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{keyword}")
    public ResponseEntity<Void> deleteOne(@RequestParam String userId, @PathVariable String keyword) {
        searchService.deleteSearchHistory(userId, keyword);
        return ResponseEntity.ok().build();
    }
}
