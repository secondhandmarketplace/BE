//package com.kdt.backend.controller;
//
//import com.kdt.backend.service.PerplexityService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/perplexity")
//@RequiredArgsConstructor
//public class PerplexityController {
//
//    private final PerplexityService perplexityService;
//
//    @PostMapping("/chat/completions")
//    public Mono<ResponseEntity<Map<String, Object>>> chat(@RequestBody Map<String, Object> request) {
//        String prompt = (String) request.get("prompt");
//        return perplexityService.chat(prompt)
//                .map(ResponseEntity::ok);
//    }
//}