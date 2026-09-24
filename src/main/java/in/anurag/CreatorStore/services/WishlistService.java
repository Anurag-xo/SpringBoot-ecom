package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

  private final WishlistRepository wishlistRepository;
  private final ProductRepository productRepository;

  @Transactional
  public Wishlist addToWishlist(Long productId, User user) {
    // Check if product exists
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    // Check if already in wishlist
    if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
      throw new RuntimeException("Product is already in your wishlist");
    }

    Wishlist wishlist = Wishlist.builder().user(user).product(product).build();

    return wishlistRepository.save(wishlist);
  }

  @Transactional
  public void removeFromWishlist(Long productId, User user) {
    if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
      throw new ResourceNotFoundException("Product not found in your wishlist");
    }

    wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
  }

  public List<Wishlist> getUserWishlist(User user) {
    return wishlistRepository.findByUserIdOrderByAddedAtDesc(user.getId());
  }

  public boolean isInWishlist(Long productId, User user) {
    return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
  }
}
