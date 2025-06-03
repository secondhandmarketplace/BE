package com.kdt.backend.service;

import com.kdt.backend.dto.FavoriteItemDTO;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemLike;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ItemLikeRepository;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemLikeService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemLikeRepository itemLikeRepository;

    public void likeItem(Long itemId, String userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (itemLikeRepository.existsByItemAndUser(item, user)) return;

        ItemLike like = new ItemLike();
        like.setItem(item);
        like.setUser(user);
        itemLikeRepository.save(like);
    }

    public void unlikeItem(Long itemId, String userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        itemLikeRepository.deleteByItemAndUser(item, user);
    }

    public boolean isLiked(Long itemId, String userId) {
        return itemLikeRepository.existsByItemAndUser(
                itemRepository.findById(itemId).orElseThrow(),
                userRepository.findById(userId).orElseThrow()
        );
    }

    public Long countLikes(Long itemId) {
        return itemLikeRepository.countByItem(
                itemRepository.findById(itemId).orElseThrow()
        );
    }

    public List<FavoriteItemDTO> getUserFavorites(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ItemLike> likes = itemLikeRepository.findByUser(user);

        return likes.stream()
                .map(like -> {
                    Item item = like.getItem();
                    Long count = itemLikeRepository.countByItem(item);
                    List<String> imagePaths = item.getItemImages().stream()
                            .map(image -> image.getPhotoPath())
                            .toList();

                    return new FavoriteItemDTO(
                            item.getItemid(),
                            item.getTitle(),
                            item.getPrice(),
                            count,
                            imagePaths // ✅ 여기서 이미지 경로 리스트 전달
                    );
                })
                .toList();
    }


}
