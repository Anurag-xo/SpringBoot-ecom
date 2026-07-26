package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
