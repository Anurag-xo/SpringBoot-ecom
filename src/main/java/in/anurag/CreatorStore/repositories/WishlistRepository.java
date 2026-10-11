package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

  List<Wishlist> findByUserOrderByAddedAtDesc(User user);

  // MUST BE findByUserIdAndProductId to match the Long, Long parameters
  Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
}
