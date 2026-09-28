package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Cart;
import in.anurag.CreatorStore.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByUser(User user);
}
