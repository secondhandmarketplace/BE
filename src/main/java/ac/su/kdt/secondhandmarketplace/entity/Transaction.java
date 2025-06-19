package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User buyer;
    
    @Column(name = "final_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal finalPrice;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @OneToOne
    @JoinColumn(name = "review_id2", referencedColumnName = "review_id")
    private Review review;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) { // transactionDate가 null일 경우 현재 시간으로 설정
            transactionDate = LocalDateTime.now(); // 현재 시간으로 transactionDate 초기화
        }
    }
} 