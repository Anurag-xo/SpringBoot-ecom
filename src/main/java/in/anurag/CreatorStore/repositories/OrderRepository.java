package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserOrderByCreatedAtDesc(User user);

  List<Order> findAllByOrderByCreatedAtDesc();

  // NEW: Find all PENDING orders created before a specific date
  List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime date);
}
