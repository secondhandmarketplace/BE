package com.kdt.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String userid; // 기본키(PK), 사용자 아이디

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String status;

    private String password;

    @Column(name = "manner_score")
    private Double mannerScore;

    // 연관 관계
    @OneToMany(mappedBy = "seller")
    @JsonBackReference
    private List<Item> sellingItems;

    @OneToMany(mappedBy = "buyer")
    private List<Item> buyingItems;

    @OneToMany(mappedBy = "user")
    private List<ItemTransaction> transactions;

    @OneToMany(mappedBy = "reporter")
    private List<Report> reportsSent;

    @OneToMany(mappedBy = "reported")
    private List<Report> reportsReceived;

    @OneToMany(mappedBy = "blocker")
    private List<Block> blockedUsers;

    @OneToMany(mappedBy = "blocked")
    private List<Block> blockingUsers;

    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> sentMessages;


}