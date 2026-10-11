package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.dto.OrderStatusUpdateRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

  private final OrderService orderService;
  private final UserRepository userRepository;

  @Operation(summary = "Create a new order")
  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    return ResponseEntity.ok(orderService.createOrder(orderRequest, getCurrentUser()));
  }

  @Operation(summary = "Get all orders for current user")
  @GetMapping("/my-orders")
  public ResponseEntity<List<Order>> getMyOrders() {
    return ResponseEntity.ok(orderService.getOrdersByUser(getCurrentUser()));
  }

  @Operation(summary = "Get all orders (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<Order>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  @Operation(summary = "Get order by ID")
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getOrderById(id, getCurrentUser()));
  }

  @Operation(summary = "Get order by ID (Admin)")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/{id}")
  public ResponseEntity<Order> getOrderByIdForAdmin(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getOrderByIdForAdmin(id));
  }

  @Operation(summary = "Update order status (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<Order> updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
    return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
  }

  @Operation(summary = "Cancel an order")
  @DeleteMapping("/{id}")
  public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.cancelOrder(id, getCurrentUser()));
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
