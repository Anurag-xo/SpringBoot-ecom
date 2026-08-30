package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
    return ResponseEntity.ok(orderService.createOrder(orderRequest));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
    // You can add orderRepository.findById(id) here, or add a method in OrderService
    // For simplicity, assuming you add getOrderById to OrderService similar to ProductService
    return ResponseEntity.ok().build(); // Placeholder
  }
}
