package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  // NEW: Group by product, sum quantity and revenue, order by most sold
  @Query(
      "SELECT p.name, SUM(oi.quantity), SUM(oi.priceAtPurchase * oi.quantity) "
          + "FROM OrderItem oi JOIN oi.product p JOIN oi.order o "
          + "WHERE o.status <> 'CANCELLED' AND o.createdAt >= :startDate "
          + "GROUP BY p.id, p.name ORDER BY SUM(oi.quantity) DESC")
  List<Object[]> findTopSellingProducts(
      @Param("startDate") LocalDateTime startDate, Pageable pageable);
}
