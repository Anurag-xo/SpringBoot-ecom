package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderItem;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
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

  // Create order and link it to the authenticated user
  @Transactional
  public Order createOrder(OrderRequest orderRequest, User user) {
    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;

    Order order = new Order();
    order.setCustomerName(orderRequest.getCustomerName());
    order.setCustomerEmail(orderRequest.getCustomerEmail());
    order.setStatus("CONFIRMED");
    order.setUser(user); // Link order to user

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

      BigDecimal itemTotal =
          product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      totalPrice = totalPrice.add(itemTotal);

      product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
      productRepository.save(product);

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

    return orderRepository.save(order);
  }

  // Get orders for a specific user
  public List<Order> getOrdersByUser(User user) {
    return orderRepository.findByUserOrderByCreatedAtDesc(user);
  }

  // Get all orders (for admin)
  public List<Order> getAllOrders() {
    return orderRepository.findAllByOrderByCreatedAtDesc();
  }

  // Get a single order by ID (with user verification)
  public Order getOrderById(Long id, User user) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

    // Verify the order belongs to this user
    if (!order.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You don't have permission to view this order");
    }

    return order;
  }

  // Get a single order by ID (for admin - no user verification)
  public Order getOrderByIdForAdmin(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
  }
}
