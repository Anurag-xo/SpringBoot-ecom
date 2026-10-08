package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.ProductVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

  List<ProductVariant> findByProductId(Long productId);

  List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

  Optional<ProductVariant> findBySku(String sku);

  boolean existsBySku(String sku);

  List<ProductVariant> findByProductIdAndSizeIgnoreCaseAndColorIgnoreCase(
      Long productId, String size, String color);
}
