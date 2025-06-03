package com.kdt.backend.service;

import com.kdt.backend.dto.ReviewRequestDTO;
import com.kdt.backend.entity.Item;
import com.kdt.backend.entity.ItemTransaction;
import com.kdt.backend.entity.Review;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ItemRepository;
import com.kdt.backend.repository.ItemTransactionRepository;
import com.kdt.backend.repository.ReviewRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kdt.backend.dto.ReviewResponseDTO;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemTransactionRepository itemTransactionRepository;


    public List<ReviewResponseDTO> getReviewsForSeller(String sellerId) {
        return reviewRepository.findByReviewee_Userid(sellerId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void createReview(ReviewRequestDTO dto) {
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        User buyer = userRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));
        User reviewee = userRepository.findById(dto.getRevieweeId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewee not found"));
        ItemTransaction transaction = itemTransactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (reviewRepository.existsByItemAndBuyer(item, buyer)) {
            throw new IllegalStateException("이미 리뷰를 작성했습니다.");
        }

        Review review = new Review();
        review.setItem(item);
        review.setBuyer(buyer);
        review.setReviewee(reviewee);
        review.setReviewer(buyer); // 리뷰 작성자는 보통 구매자
        review.setTransaction(transaction);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        reviewRepository.save(review);
    }
}
