package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/** OrderitemRepository */
public interface OrderitemRepository extends JpaRepository<OrderItem, Long> {}
