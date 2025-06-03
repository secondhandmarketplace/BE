//package com.kdt.backend.service;
//
//import com.kdt.backend.entity.Item;
//import com.kdt.backend.repository.ItemRepository;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class AiQueryService {
//    private final ItemRepository itemRepository;
//    private final OpenAiChatModel chatModel;
//
//    public AiQueryService(ItemRepository itemRepository, OpenAiChatModel chatModel) {
//        this.itemRepository = itemRepository;
//        this.chatModel = chatModel;
//    }
//
//    public Map<String, Object> askWithItems(String userPrompt) {
//        List<Item> items = itemRepository.findAll();
//        if (items.isEmpty()) {
//            return Map.of("answer", "DB에 게시글이 없습니다.");
//        }
//
//        Item cheapest = items.stream()
//                .min(Comparator.comparingInt(Item::getPrice))
//                .orElse(null);
//
//        if (cheapest == null) {
//            return Map.of("answer", "DB에 게시글이 없습니다.");
//        }
//
//        String promptText = userPrompt +
//                "\n\n[가장 싼 게시글]\n" +
//                "제목: " + (cheapest.getTitle() != null ? cheapest.getTitle() : "") +
//                "\n가격: " + cheapest.getPrice() + "원" +
//                "\n설명: " + (cheapest.getDescription() != null ? cheapest.getDescription() : "") +
//                "\n카테고리: " + (cheapest.getCategory() != null ? cheapest.getCategory() : "");
//
//        List<Message> messages = List.of(new UserMessage(promptText));
//        Prompt prompt = new Prompt(messages);
//
//        // AI 응답 추출 (버전에 따라 getOutput().toString() 또는 getText() 등)
//        var chatResponse = chatModel.call(prompt);
//        String aiAnswer = chatResponse.getResult().getOutput() != null
//                ? chatResponse.getResult().getOutput().toString()
//                : "";
//
//        String itemTitle = cheapest.getTitle() != null ? cheapest.getTitle() : "";
//        String link = "/items/" + cheapest.getItemid();
//        String thumbnail = cheapest.getThumbnail() != null ? cheapest.getThumbnail() : "";
//
//        // Null 안전하게 HashMap 사용
//        Map<String, Object> result = new HashMap<>();
//        result.put("answer", aiAnswer);
//        result.put("itemTitle", itemTitle);
//        result.put("itemLink", link);
//        result.put("thumbnail", thumbnail);
//        return result;
//    }
//}
