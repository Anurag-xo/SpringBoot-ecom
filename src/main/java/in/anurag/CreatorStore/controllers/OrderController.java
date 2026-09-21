package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.dto.OrderStatusUpdateRequest;
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

  // 1. Create a new order (Authenticated users only)
  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.createOrder(orderRequest, currentUser));
  }

  // 2. Get current user's order history
  @GetMapping("/my-orders")
  public ResponseEntity<List<Order>> getMyOrders() {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.getOrdersByUser(currentUser));
  }

  // 3. Get a specific order by ID (User must own it)
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.getOrderById(id, currentUser));
  }

  // 4. ADMIN ONLY: Get all orders in the system
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<Order>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  // 5. ADMIN ONLY: Update the status of an order (e.g., PENDING -> SHIPPED)
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}/status")
  public ResponseEntity<Order> updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {

    return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
  }

  // 6. USER: Cancel their own order (Service handles the PENDING check and stock restoration)
  @PutMapping("/{id}/cancel")
  public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
    User currentUser = getCurrentUser();
    return ResponseEntity.ok(orderService.cancelOrder(id, currentUser));
  }

  // ==========================================
  // Helper method to extract the logged-in user
  // ==========================================
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
