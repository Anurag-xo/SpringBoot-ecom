package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByProductId(Long productId);

  // ✅ NEW: Soft delete query
  @Modifying
  @Query("UPDATE Review r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
  void softDeleteById(@Param("id") Long id);
}
