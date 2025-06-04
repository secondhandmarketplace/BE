package ac.su.kdt.secondhandmarketplace.repository;

import ac.su.kdt.secondhandmarketplace.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}