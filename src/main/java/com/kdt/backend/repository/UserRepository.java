package com.kdt.backend.repository;

import com.kdt.backend.entity.User; // ✅ 요거 중요!
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUserid(String userid);

    boolean existsByEmail(String email);

    Optional<User> findByUserid(String userid);
}