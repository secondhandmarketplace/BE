package com.kdt.backend.repository;

import com.kdt.backend.entity.User; // ✅ 요거 중요!
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
