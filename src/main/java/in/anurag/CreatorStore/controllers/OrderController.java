package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** OrderController */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public Order createOrder(@Valid @RequestBody OrderRequest OrderRequest) {
    return orderService.createOrder(OrderRequest);
  }
}
