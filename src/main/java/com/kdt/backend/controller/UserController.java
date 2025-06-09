package com.kdt.backend.controller;

import com.kdt.backend.entity.User;
import com.kdt.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    /**
     * ✅ 사용자 존재 확인 (검색 결과 [1] 참조 - 명확한 경로 지정)
     */
    @GetMapping("/auth/check/userid/{userid}")  // ✅ 명확한 경로 지정
    public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String userid) {
        try {
            log.info("사용자 존재 확인 요청: userid={}", userid);

            boolean exists = userService.existsByUserid(userid);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("userId", userid);
            response.put("timestamp", LocalDateTime.now());
            response.put("success", true);

            log.info("사용자 존재 확인 완료: userId={}, exists={}", userid, exists);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 존재 확인 실패: userId={}, error={}", userid, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exists", false);
            errorResponse.put("userId", userid);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    /**
     * ✅ 이메일 중복 확인
     */
    @GetMapping("/auth/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@PathVariable String email) {
        try {
            log.info("이메일 중복 확인 요청: email={}", email);

            boolean exists = userService.existsByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("email", email);
            response.put("timestamp", LocalDateTime.now());
            response.put("success", true);

            log.info("이메일 중복 확인 완료: email={}, exists={}", email, exists);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이메일 중복 확인 실패: email={}, error={}", email, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exists", false);
            errorResponse.put("email", email);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> signupRequest) {
        try {
            String userid = signupRequest.get("userid");
            String name = signupRequest.get("name");
            String email = signupRequest.get("email");
            String phone = signupRequest.get("phone");
            String password = signupRequest.get("password");

            log.info("회원가입 요청: userId={}, email={}", userid, email);

            // 중복 확인
            if (userService.existsByUserid(userid)) {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "이미 존재하는 아이디입니다."
                ));
            }

            if (userService.existsByEmail(email)) {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "이미 존재하는 이메일입니다."
                ));
            }

            // 비밀번호 암호화
            String encodedPassword = userService.encodePassword(password);

            // 사용자 생성
            User newUser = User.builder()
                    .userid(userid)
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .password(encodedPassword)
                    .status("active")
                    .mannerScore(5.0)
                    .build();

            userService.save(newUser);

            log.info("회원가입 성공: userId={}", userid);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원가입이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "회원가입 처리 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    //TODO : 로그인 버튼 클릭 시 사용자 인증 로직 구현



    /**
     * ✅ 사용자 정보 조회 ->loginPage.jsx (56번줄)에서 사용
     */
    @GetMapping("/users/check/${userid}")  // ✅ /check와 구분되는 명확한 경로
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String userid) {
        try {
            log.info("사용자 프로필 조회: userId={}", userid);

            User user = userService.findById(userid);
            if (user == null) {
                Map<String, Object> notFoundResponse = new HashMap<>();
                notFoundResponse.put("found", false);
                notFoundResponse.put("message", "사용자를 찾을 수 없습니다.");
                notFoundResponse.put("userId", userid);

                return ResponseEntity.status(404).body(notFoundResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("found", true);
            response.put("user", user);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 프로필 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ 로그인 처리 (검색 결과 [4] 참조 - userId 응답에 포함)
     */
    @PostMapping("/users/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String userid = loginRequest.get("userid");
            String password = loginRequest.get("password");

            log.info("로그인 시도: userId={}", userid);

            // ✅ 사용자 인증
            boolean authenticated = userService.authenticate(userid, password);

            if (authenticated) {
                // ✅ 사용자 정보 조회
                User user = userService.findById(userid);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("userId", userid); // ✅ 검색 결과 [4] 패턴: userId 명시적 포함
                response.put("userName", user != null ? user.getName() : userid);
                response.put("message", "로그인 성공");
                response.put("timestamp", LocalDateTime.now());

                // ✅ JWT 토큰 생성 (필요시)
                String token = generateToken(userid);
                response.put("token", token);

                log.info("로그인 성공: userId={}", userid);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");

                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            log.error("로그인 처리 실패: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다.");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    /**
     * ✅ 서비스 상태 확인 (검색 결과 [3] 참조 - 로그 확인용)
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "user-controller");
        status.put("status", "active");
        status.put("timestamp", LocalDateTime.now());
        status.put("endpoints", new String[]{
                "/check/{userId}", "/profile/{userId}", "/login", "/status"
        });

        return ResponseEntity.ok(status);
    }

    // ✅ 헬퍼 메서드
    private String generateToken(String userId) {
        // JWT 토큰 생성 로직 (실제 구현 필요)
        return "token_" + userId + "_" + System.currentTimeMillis();
    }
}