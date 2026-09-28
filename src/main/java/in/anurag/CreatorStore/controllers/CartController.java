package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.AddToCartRequest;
import in.anurag.CreatorStore.dto.UpdateCartItemRequest;
import in.anurag.CreatorStore.entities.Cart;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<Cart> getCart() {
    return ResponseEntity.ok(cartService.getCart(getCurrentUser()));
  }

  @PostMapping("/items")
  public ResponseEntity<Cart> addToCart(@Valid @RequestBody AddToCartRequest request) {
    return ResponseEntity.ok(cartService.addToCart(request, getCurrentUser()));
  }

  @PutMapping("/items/{itemId}")
  public ResponseEntity<Cart> updateCartItem(
      @PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest request) {
    return ResponseEntity.ok(cartService.updateCartItem(itemId, request, getCurrentUser()));
  }

  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Cart> removeCartItem(@PathVariable Long itemId) {
    return ResponseEntity.ok(cartService.removeCartItem(itemId, getCurrentUser()));
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart() {
    cartService.clearCart(getCurrentUser());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/checkout")
  public ResponseEntity<Order> checkout() {
    return ResponseEntity.ok(cartService.checkout(getCurrentUser()));
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
