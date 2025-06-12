package com.kdt.backend.service;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.ChatRoom;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.User;
import com.kdt.backend.exception.NotFoundException;
import com.kdt.backend.repository.ChatMessageRepository;
import com.kdt.backend.repository.ChatRoomRepository;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponseDTO createOrGetChatRoom(String buyerId, String sellerId, Long itemTransactionId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new NotFoundException("구매자를 찾을 수 없습니다: " + buyerId));
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("판매자를 찾을 수 없습니다: " + sellerId));
        Item item = itemRepository.findById(itemTransactionId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다: " + itemTransactionId));

        Optional<ChatRoom> existingRoomOpt = chatRoomRepository.findByItemAndUsers(item, buyer, seller);

        ChatRoom chatRoom = existingRoomOpt.orElseGet(() -> {
            log.info("새로운 채팅방을 생성합니다. Buyer: {}, Seller: {}, Item: {}", buyerId, sellerId, itemTransactionId);
            ChatRoom newRoom = ChatRoom.builder()
                    .buyer(buyer)
                    .seller(seller)
                    .itemTransaction(item) // ✅ 올바른 빌더 메서드
                    .build();
            return chatRoomRepository.save(newRoom);
        });

        return convertToDto(chatRoom, buyer.getUserid());
    }

    public List<ChatRoomResponseDTO> getChatRoomsByUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
        List<ChatRoom> rooms = chatRoomRepository.findChatRoomsByUser(userId);
        return rooms.stream()
                .map(room -> convertToDto(room, userId))
                .collect(Collectors.toList());
    }

    public ChatRoomResponseDTO getChatRoomDetails(Long chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다: " + chatRoomId));
        return convertToDto(chatRoom, userId);
    }

    private ChatRoomResponseDTO convertToDto(ChatRoom chatRoom, String currentUserId) {
        User otherUser = chatRoom.getBuyer().getUserid().equals(currentUserId)
                ? chatRoom.getSeller()
                : chatRoom.getBuyer();

        String lastMessage = chatMessageRepository.findFirstByChatRoom_IdOrderBySentAtDesc(chatRoom.getId())
                .map(msg -> msg.getContent())
                .orElse("대화를 시작해보세요.");

        return ChatRoomResponseDTO.builder()
                .id(chatRoom.getId()) // ✅ id() 메서드 사용
                .otherUserId(otherUser.getUserid())
                .otherUserName(otherUser.getName())
                // .otherUserPicture(otherUser.getPicture()) // User 엔티티에 해당 필드가 없다면 주석 처리
                .lastMessage(lastMessage)
                .updatedAt(chatRoom.getUpdatedAt())
                .unreadCount(chatRoom.getUnreadCount())
                .status(chatRoom.getStatus())
                .itemTransactionId(chatRoom.getItemTransaction().getItemid()) // ✅ getItemid() 사용
                .itemTitle(chatRoom.getItemTransaction().getTitle())
                .itemPrice(chatRoom.getItemTransaction().getPrice())
                .itemImageUrl(chatRoom.getItemTransaction().getThumbnail())
                .build();
    }
}
