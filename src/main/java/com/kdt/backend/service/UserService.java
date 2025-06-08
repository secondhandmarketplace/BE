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
    public boolean authenticate(String userid, String rawPassword) {
        try {
            log.info("사용자 인증 시도: userId={}", userid);

            User user = userRepository.findById(userid).orElse(null);
            if (user == null) {
                log.warn("사용자를 찾을 수 없음: {}", userid);
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

            log.info("사용자 인증 결과: userId={}, success={}", userid, matches);
            return matches;

        } catch (Exception e) {
            log.error("사용자 인증 중 오류: userId={}, error={}", userid, e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 사용자 존재 확인
     */
    public boolean existsByUserid(String userid) {
        try {
            boolean exists = userRepository.existsByUserid(userid);
            log.info("사용자 존재 확인: userId={}, exists={}", userid, exists);
            return exists;
        } catch (Exception e) {
            log.error("사용자 존재 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ 사용자 조회
     */
    public User findById(String userid) {
        try {
            return userRepository.findById(userid).orElse(null);
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

    public boolean existsByEmail(String email) {
        try {
            boolean exists = userRepository.existsByEmail(email);
            log.info("이메일 존재 확인: email={}, exists={}", email, exists);
            return exists;
        } catch (Exception e) {
            log.error("이메일 존재 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    public void save(User newUser) {
        try {
            userRepository.save(newUser);
            log.info("사용자 저장 성공: userId={}", newUser.getUserid());
        } catch (Exception e) {
            log.error("사용자 저장 중 오류: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 호출자에게 알림
        }
    }
}