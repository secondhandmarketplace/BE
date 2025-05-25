package ac.su.kdt.secondhandmarketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "university")
public class University {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "email_domain", nullable = false, length = 50)
    private String emailDomain;
} 