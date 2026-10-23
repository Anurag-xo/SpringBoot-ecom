package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.UserDataExportDto;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Review;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ReviewRepository;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.repositories.WishlistRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final ReviewRepository reviewRepository;
  private final WishlistRepository wishlistRepository;
  private final PasswordEncoder passwordEncoder;

  // ✅ GDPR: Export all user data
  public UserDataExportDto exportUserData(User user) {
    List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
    List<Review> reviews = reviewRepository.findByUserId(user.getId());
    List<Wishlist> wishlists = wishlistRepository.findByUserOrderByAddedAtDesc(user);

    return UserDataExportDto.builder()
        .user(user)
        .orders(orders)
        .reviews(reviews)
        .wishlists(wishlists)
        .build();
  }

  // ✅ GDPR: Right to be Forgotten (Anonymize & Soft Delete)
  @Transactional
  public void deleteAccount(User user) {
    // 1. Anonymize Orders (Keep for accounting/audit, but remove PII)
    List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
    for (Order order : orders) {
      order.setCustomerName("Deleted User");
      order.setCustomerEmail("deleted@anon.com");
      orderRepository.save(order);
    }

    // 2. Soft delete reviews
    List<Review> reviews = reviewRepository.findByUserId(user.getId());
    for (Review review : reviews) {
      reviewRepository.softDeleteById(review.getId());
    }

    // 3. Clear wishlists
    List<Wishlist> wishlists = wishlistRepository.findByUserOrderByAddedAtDesc(user);
    wishlistRepository.deleteAll(wishlists);

    // 4. Anonymize User Profile
    String anonSuffix = "_" + user.getId();
    user.setUsername("deleted_user" + anonSuffix);
    user.setEmail("deleted" + anonSuffix + "@anon.com");
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    user.setEnabled(false); // Prevent any further login attempts

    // 5. Soft delete the user
    userRepository.softDeleteById(user.getId());
  }
}
