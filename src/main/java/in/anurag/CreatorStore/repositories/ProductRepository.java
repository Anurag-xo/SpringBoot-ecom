package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

  Page<Product> findByNameContainingIgnoreCase(String search, Pageable pageable);

  Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(
      String category, String search, Pageable pageable);

  // NEW: Find all products with stock quantity less than the given amount
  List<Product> findByStockQuantityLessThan(int quantity);
}
