package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderItem;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  @Transactional
  public Order createOrder(OrderRequest orderRequest) {
    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;

    Order order = new Order();
    order.setCustomerName(orderRequest.getCustomerName());
    order.setCustomerEmail(orderRequest.getCustomerEmail());
    order.setStatus("CONFIRMED");

    for (OrderItemRequest itemRequest : orderRequest.getItems()) {
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found with id: " + itemRequest.getProductId()));

      if (product.getStockQuantity() < itemRequest.getQuantity()) {
        throw new RuntimeException(
            "Not enough stock for product id: " + itemRequest.getProductId());
      }

      // Calculate price and update stock
      BigDecimal itemTotal =
          product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      totalPrice = totalPrice.add(itemTotal);

      product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
      productRepository.save(product); // Save updated stock

      // Build Order Item
      OrderItem orderItem =
          OrderItem.builder()
              .order(order)
              .product(product)
              .quantity(itemRequest.getQuantity())
              .priceAtPurchase(product.getPrice())
              .build();

      orderItems.add(orderItem);
    }

    order.setTotalPrice(totalPrice);
    order.setOrderItems(orderItems);

    return orderRepository.save(order); // Save the complete order
  }
}
