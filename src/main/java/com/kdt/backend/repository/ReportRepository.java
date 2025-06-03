package com.kdt.backend.repository;

import com.kdt.backend.entity.Report;
import com.kdt.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByReported(User reported);
}
