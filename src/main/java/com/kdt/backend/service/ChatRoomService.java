package com.kdt.backend.service;

import com.kdt.backend.dto.ChatRoomResponseDTO;
import com.kdt.backend.entity.*;
import com.kdt.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ItemTransactionRepository itemTransactionRepository;
    private final UserRepository userRepository;

    public ChatRoomResponseDTO createChatRoom(Long transactionId, String buyerId, String sellerId) {
        ItemTransaction transaction = itemTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래를 찾을 수 없습니다."));
        Item item = transaction.getItem();

        ChatRoom room = chatRoomRepository
                .findByItemTransaction_Transactionid(transactionId)
                .orElseGet(() -> {
                    User buyer = userRepository.findById(buyerId)
                            .orElseThrow(() -> new RuntimeException("구매자 정보를 찾을 수 없습니다."));
                    User seller = userRepository.findById(sellerId)
                            .orElseThrow(() -> new RuntimeException("판매자 정보를 찾을 수 없습니다."));

                    ChatRoom newRoom = ChatRoom.builder()
                            .itemTransaction(transaction)
                            .buyer(buyer)
                            .seller(seller)
                            .createdAt(System.currentTimeMillis())
                            .build();

                    return chatRoomRepository.save(newRoom);
                });

        // ✅ 최신 메시지 조회 (빈 문자열로 초기화)
        String latestMessage = chatMessageRepository
                .findFirstByChatRoom_ChatroomidOrderBySentAtDesc(room.getChatroomid())
                .map(ChatMessage::getContent)
                .orElse("");

        return new ChatRoomResponseDTO(
                room.getChatroomid(),
                item.getItemid(),
                item.getTitle(),
                room.getBuyer().getUserid(),
                room.getBuyer().getName(),
                room.getSeller().getName(),
                room.getSeller().getUserid(),
                room.getCreatedAt(),
                latestMessage  // ✅ 추가
        );
    }

    public List<ChatRoomResponseDTO> getChatRoomsByUser(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByBuyer_UseridOrSeller_Userid(userId, userId);

        return rooms.stream()
                .map(room -> {
                    // ✅ 최신 메시지 조회
                    String latestMessage = chatMessageRepository
                            .findFirstByChatRoom_ChatroomidOrderBySentAtDesc(room.getChatroomid())
                            .map(ChatMessage::getContent)
                            .orElse("");

                    return new ChatRoomResponseDTO(
                            room.getChatroomid(),
                            room.getItemTransaction().getItem().getItemid(),
                            room.getItemTransaction().getItem().getTitle(),
                            room.getBuyer().getUserid(),
                            room.getBuyer().getName(),
                            room.getSeller().getName(),
                            room.getSeller().getUserid(),
                            room.getCreatedAt(),
                            latestMessage  // ✅ 추가
                    );
                })
                .collect(Collectors.toList());
    }

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        chatMessageRepository.deleteAllByChatRoom_Chatroomid(chatRoomId);
        chatRoomRepository.deleteById(chatRoomId);
    }

    public String getLatestMessageByRoomId(Long chatRoomId) {
        return chatMessageRepository
                .findFirstByChatRoom_ChatroomidOrderBySentAtDesc(chatRoomId)
                .map(ChatMessage::getContent)
                .orElse("");
    }

}
