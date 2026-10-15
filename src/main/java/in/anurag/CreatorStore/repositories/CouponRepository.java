package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
  Optional<Coupon> findByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCase(String code);
}
