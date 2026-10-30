package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.AddressType;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.UserAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    List<UserAddress> findByUserAndAddressType(User user, AddressType type);
    Optional<UserAddress> findByUserAndIsDefaultTrueAndAddressType(User user, AddressType type);
}
