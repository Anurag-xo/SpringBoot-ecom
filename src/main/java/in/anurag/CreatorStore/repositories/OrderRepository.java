package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserOrderByCreatedAtDesc(User user);

  List<Order> findAllByOrderByCreatedAtDesc();

  List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime date);

  // NEW: Aggregate total revenue and order count, ignoring cancelled orders
  @Query(
      "SELECT SUM(o.totalPrice), COUNT(o) FROM Order o WHERE o.status <> 'CANCELLED' AND"
          + " o.createdAt >= :startDate")
  Object[] getRevenueStats(@Param("startDate") LocalDateTime startDate);
}
