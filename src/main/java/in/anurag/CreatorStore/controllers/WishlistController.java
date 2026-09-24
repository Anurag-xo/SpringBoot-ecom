package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.WishlistService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

  private final WishlistService wishlistService;
  private final UserRepository userRepository;

  // 1. Add product to wishlist
  @PostMapping("/{productId}")
  public ResponseEntity<Wishlist> addToWishlist(@PathVariable Long productId) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(wishlistService.addToWishlist(productId, currentUser));
  }

  // 2. Remove product from wishlist
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
    User currentUser = getCurrentUser();
    wishlistService.removeFromWishlist(productId, currentUser);
    return ResponseEntity.noContent().build();
  }

  // 3. Get user's wishlist
  @GetMapping
  public ResponseEntity<List<Wishlist>> getUserWishlist() {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(wishlistService.getUserWishlist(currentUser));
  }

  // 4. Check if product is in wishlist
  @GetMapping("/{productId}/check")
  public ResponseEntity<Map<String, Boolean>> isInWishlist(@PathVariable Long productId) {
    User currentUser = getCurrentUser();
    boolean isInWishlist = wishlistService.isInWishlist(productId, currentUser);

    Map<String, Boolean> response = new HashMap<>();
    response.put("inWishlist", isInWishlist);

    return ResponseEntity.ok(response);
  }

  // Helper method to extract the logged-in user
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
