package com.kdt.backend.service;

import com.kdt.backend.entity.User;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ✅ PasswordEncoder 주입

    /**
     * ✅ 사용자 인증 (평문 비밀번호 비교)
     */
    public boolean authenticate(String userId, String rawPassword) {
        try {
            log.info("사용자 인증 시도: userId={}", userId);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("사용자를 찾을 수 없음: {}", userId);
                return false;
            }

            // ✅ 평문 비밀번호와 DB 비밀번호 비교
            boolean matches;
            if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
                // 암호화된 비밀번호인 경우
                matches = passwordEncoder.matches(rawPassword, user.getPassword());
            } else {
                // 평문 비밀번호인 경우 (개발 환경)
                matches = rawPassword.equals(user.getPassword());
            }

            log.info("사용자 인증 결과: userId={}, success={}", userId, matches);
            return matches;

        } catch (Exception e) {
            log.error("사용자 인증 중 오류: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 사용자 존재 확인
     */
    public boolean existsById(String userId) {
        try {
            boolean exists = userRepository.existsById(userId);
            log.info("사용자 존재 확인: userId={}, exists={}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("사용자 존재 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 사용자 조회
     */
    public User findById(String userId) {
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            log.error("사용자 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ 비밀번호 암호화 (회원가입 시 사용)
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
