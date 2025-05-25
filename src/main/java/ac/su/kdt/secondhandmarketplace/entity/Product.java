package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price", nullable = false)
    private Integer price;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
    
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms;
} 