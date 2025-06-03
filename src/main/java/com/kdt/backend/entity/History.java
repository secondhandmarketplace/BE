package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @Column(name = "search_at")
    private LocalDateTime searchAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_userid")
    private User user;
}
