package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  // get all orders for a specific user
  List<Order> findByUserOrderByCreatedAtDesc(User user);

  // get all orders (for admin view)
  List<Order> findAllByOrderByCreatedAtDesc();
}
