package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.AddToCartRequest;
import in.anurag.CreatorStore.dto.UpdateCartItemRequest;
import in.anurag.CreatorStore.entities.Cart;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart") // ✅ UPDATED: Added v1 versioning
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
public class CartController {

  private final CartService cartService;
  private final UserRepository userRepository;

  @Operation(summary = "Get current user's cart")
  @GetMapping
  public ResponseEntity<Cart> getCart() {
    return ResponseEntity.ok(cartService.getCart(getCurrentUser()));
  }

  @Operation(summary = "Add item to cart")
  @PostMapping("/items")
  public ResponseEntity<Cart> addToCart(@Valid @RequestBody AddToCartRequest request) {
    return ResponseEntity.ok(cartService.addToCart(request, getCurrentUser()));
  }

  @Operation(summary = "Update cart item quantity")
  @PutMapping("/items/{itemId}")
  public ResponseEntity<Cart> updateCartItem(
      @PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest request) {
    return ResponseEntity.ok(cartService.updateCartItem(itemId, request, getCurrentUser()));
  }

  @Operation(summary = "Remove item from cart")
  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Cart> removeCartItem(@PathVariable Long itemId) {
    return ResponseEntity.ok(cartService.removeCartItem(itemId, getCurrentUser()));
  }

  @Operation(summary = "Clear entire cart")
  @DeleteMapping
  public ResponseEntity<Void> clearCart() {
    cartService.clearCart(getCurrentUser());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Checkout - Convert cart to order")
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
