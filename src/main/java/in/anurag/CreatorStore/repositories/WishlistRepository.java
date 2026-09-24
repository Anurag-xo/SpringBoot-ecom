package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

  // Get all wishlist items for a user, newest first
  List<Wishlist> findByUserIdOrderByAddedAtDesc(Long userId);

  // Check if a specific product is in a user's wishlist
  Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

  // Check if a product exists in user's wishlist (returns boolean)
  boolean existsByUserIdAndProductId(Long userId, Long productId);

  // Delete a specific wishlist item
  void deleteByUserIdAndProductId(Long userId, Long productId);
}
