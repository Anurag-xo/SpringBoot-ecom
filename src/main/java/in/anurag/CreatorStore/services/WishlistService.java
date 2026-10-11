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

  public List<Wishlist> getWishlistByUser(User user) {
    return wishlistRepository.findByUserOrderByAddedAtDesc(user);
  }

  @Transactional
  public Wishlist addToWishlist(Long productId, User user) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    // MUST use findByUserIdAndProductId here
    if (wishlistRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
      throw new RuntimeException("Product already in wishlist");
    }

    Wishlist wishlist = Wishlist.builder().user(user).product(product).build();

    return wishlistRepository.save(wishlist);
  }

  @Transactional
  public void removeFromWishlist(Long productId, User user) {
    // MUST use findByUserIdAndProductId here
    Wishlist wishlist =
        wishlistRepository
            .findByUserIdAndProductId(user.getId(), productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));

    wishlistRepository.delete(wishlist);
  }
}
