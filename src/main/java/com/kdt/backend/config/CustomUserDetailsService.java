package com.kdt.backend.service;

import com.kdt.backend.entity.User;
import com.kdt.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * ✅ DB에서 사용자 조회 (검색 결과 [4] 참조)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("사용자 인증 시도: username={}", username);

        try {
            User user = userRepository.findById(username)
                    .orElseThrow(() -> {
                        log.warn("사용자를 찾을 수 없음: {}", username);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                    });

            log.info("사용자 인증 성공: username={}", username);

            // ✅ Spring Security UserDetails 반환
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserid())
                    .password(user.getPassword()) // DB에 저장된 암호화된 비밀번호
                    .authorities("ROLE_USER")
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();

        } catch (Exception e) {
            log.error("사용자 인증 중 오류 발생: {}", e.getMessage());
            throw new UsernameNotFoundException("인증 처리 중 오류가 발생했습니다", e);
        }
    }
}
