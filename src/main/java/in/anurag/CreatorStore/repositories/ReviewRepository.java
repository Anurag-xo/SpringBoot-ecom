package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  // This is the method your ReviewService is looking for
  List<Review> findByProductId(Long productId);
}
