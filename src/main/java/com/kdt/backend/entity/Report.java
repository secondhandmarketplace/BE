package com.kdt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_userid", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_userid", nullable = false)
    private User reported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_itemid")
    private Item item;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    private Long regdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        접수, 처리중, 완료
    }
}
