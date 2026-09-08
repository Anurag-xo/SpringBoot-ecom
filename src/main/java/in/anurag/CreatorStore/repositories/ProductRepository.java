package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  // Filter by category
  Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

  // Search by product name
  Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

  // Filter by category AND search by name
  Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(
      String category, String name, Pageable pageable);
}
