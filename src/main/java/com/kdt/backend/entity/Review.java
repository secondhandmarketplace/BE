package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewid")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_itemid", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "buyer_userid", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "revieweeid", nullable = false)
    private User reviewee;

    @ManyToOne
    @JoinColumn(name = "reviewerid", nullable = false)
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "transactionid", nullable = false)  // ✅ 추가!
    private ItemTransaction transaction;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}
