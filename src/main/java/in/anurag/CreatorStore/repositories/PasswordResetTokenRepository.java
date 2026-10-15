package in.anurag.CreatorStore.repositories;

import in.anurag.CreatorStore.entities.PasswordResetToken;
import in.anurag.CreatorStore.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);

  Optional<PasswordResetToken> findByUser(User user);

  void deleteByUser(User user);
}
