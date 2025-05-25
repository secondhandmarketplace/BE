package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "category")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
} 