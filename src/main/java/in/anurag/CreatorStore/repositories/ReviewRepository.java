package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  // Get all reviews for a product, newest first
  List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

  // Check if a specific user has already reviewed a specific product
  Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

  // Calculate average rating directly in SQL for performance
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
  Double getAverageRatingByProductId(Long productId);
}
