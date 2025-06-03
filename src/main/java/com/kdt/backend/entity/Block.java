package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_userid", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_userid", nullable = false)
    private User blocked;
}
