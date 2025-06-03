package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "item_transaction")
public class ItemTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_itemid", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_userid", nullable = false)
    private User user;

    @Column(name = "distinct_seller", nullable = false)
    private String distinctSeller;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
