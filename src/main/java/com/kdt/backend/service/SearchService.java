package com.kdt.backend.service;

import com.kdt.backend.entity.History;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.SearchHistoryRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<String> getSuggestions(String userId, String keyword) {
        User user = userRepository.findById(userId).orElseThrow();
        return searchHistoryRepository
                .findTop5ByUserAndKeywordContainingOrderBySearchAtDesc(user, keyword)
                .stream()
                .map(History::getKeyword)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getSearchHistory(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return searchHistoryRepository.findAllByUserOrderBySearchAtDesc(user)
                .stream()
                .map(History::getKeyword)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addSearchHistory(String userId, String keyword) {
        User user = userRepository.findById(userId).orElseThrow();
        searchHistoryRepository.deleteByUserAndKeyword(user, keyword);
        History history = History.builder()
                .user(user)
                .keyword(keyword)
                .searchAt(LocalDateTime.now())
                .build();
        searchHistoryRepository.save(history);
    }

    @Transactional
    public void deleteSearchHistory(String userId, String keyword) {
        User user = userRepository.findById(userId).orElseThrow();
        searchHistoryRepository.deleteByUserAndKeyword(user, keyword);
    }

    @Transactional
    public void deleteAllSearchHistory(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        searchHistoryRepository.deleteAllByUser(user);
    }
}
