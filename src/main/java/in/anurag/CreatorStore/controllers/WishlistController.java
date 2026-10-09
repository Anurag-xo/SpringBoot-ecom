package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlists") // ✅ UPDATED: Added v1 versioning
@RequiredArgsConstructor
@Tag(name = "Wishlists", description = "Wishlist management endpoints")
public class WishlistController {

  private final WishlistService wishlistService;
  private final UserRepository userRepository;

  @Operation(summary = "Get current user's wishlist")
  @GetMapping
  public ResponseEntity<List<Wishlist>> getMyWishlist() {
    return ResponseEntity.ok(wishlistService.getWishlistByUser(getCurrentUser()));
  }

  @Operation(summary = "Add product to wishlist")
  @PostMapping("/products/{productId}")
  public ResponseEntity<Wishlist> addToWishlist(@PathVariable Long productId) {
    return ResponseEntity.ok(wishlistService.addToWishlist(productId, getCurrentUser()));
  }

  @Operation(summary = "Remove product from wishlist")
  @DeleteMapping("/products/{productId}")
  public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
    wishlistService.removeFromWishlist(productId, getCurrentUser());
    return ResponseEntity.noContent().build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
