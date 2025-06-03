package com.kdt.backend.controller;

import com.kdt.backend.dto.UserProfileResponseDTO;
import com.kdt.backend.entity.Review;
import com.kdt.backend.entity.User;
import com.kdt.backend.repository.ReviewRepository;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // ✅ 판매자 프로필 정보 + 평균 별점 + 후기 개수
    @GetMapping("/{userid}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@PathVariable String userid) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 평균 별점 (null이면 0.0)
        Double avg = reviewRepository.findAverageRatingByReviewee(user);
        int count = reviewRepository.countByReviewee(user);

        UserProfileResponseDTO dto = UserProfileResponseDTO.builder()
                .userid(user.getUserid())
                .name(user.getName())
                .email(user.getEmail())
                .averageRating(avg != null ? avg : 0.0)
                .reviewCount(count)
                .build();

        return ResponseEntity.ok(dto);
    }

    // ✅ 받은 후기 리스트 조회
    @GetMapping("/{userid}/reviews")
    public ResponseEntity<List<Review>> getReviewsForUser(@PathVariable String userid) {
        List<Review> reviews = reviewRepository.findByReviewee_Userid(userid);
        return ResponseEntity.ok(reviews);
    }

    // ✅ 회원가입
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userRepository.existsById(user.getUserid())) {
            throw new RuntimeException("이미 존재하는 ID입니다.");
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    // ✅ 로그인용 사용자 존재 여부 확인
    @GetMapping("/{userid}/check")
    public ResponseEntity<User> getUserById(@PathVariable String userid) {
        return userRepository.findById(userid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
