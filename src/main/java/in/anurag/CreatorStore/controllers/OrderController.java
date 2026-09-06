package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final UserRepository userRepository;

  // Create a new order (authenticated users only)
  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.createOrder(orderRequest, currentUser));
  }

  // Get current user's orders
  @GetMapping("/my-orders")
  public ResponseEntity<List<Order>> getMyOrders() {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.getOrdersByUser(currentUser));
  }

  // Get a specific order by ID (user must own it)
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.getOrderById(id, currentUser));
  }

  // ADMIN ONLY: Get all orders
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<Order>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  // ADMIN ONLY: Get any order by ID
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/{id}")
  public ResponseEntity<Order> getOrderByIdAdmin(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getOrderByIdForAdmin(id));
  }

  // Helper method to get the currently authenticated user
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
