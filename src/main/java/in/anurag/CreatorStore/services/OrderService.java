package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderItem;
import in.anurag.CreatorStore.entities.OrderStatus;
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
  private final EmailService emailService;

  @Transactional
  public Order createOrder(OrderRequest orderRequest, User user) {
    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;

    Order order = new Order();
    order.setCustomerName(orderRequest.getCustomerName());
    order.setCustomerEmail(orderRequest.getCustomerEmail());
    order.setStatus(OrderStatus.PENDING);
    order.setUser(user);

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

    Order savedOrder = orderRepository.save(order);
    emailService.sendOrderConfirmationEmail(user, savedOrder);

    return savedOrder;
  }

  public List<Order> getOrdersByUser(User user) {
    return orderRepository.findByUserOrderByCreatedAtDesc(user);
  }

  public List<Order> getAllOrders() {
    return orderRepository.findAllByOrderByCreatedAtDesc();
  }

  public Order getOrderById(Long id, User user) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You don't have permission to view this order");
    }

    return order;
  }

  public Order getOrderByIdForAdmin(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
  }

  @Transactional
  public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId));

    order.setStatus(newStatus);
    return orderRepository.save(order);
  }

  @Transactional
  public Order cancelOrder(Long orderId, User currentUser) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId));

    if (!order.getUser().getId().equals(currentUser.getId())) {
      throw new RuntimeException("You do not have permission to cancel this order");
    }

    if (order.getStatus() != OrderStatus.PENDING) {
      throw new RuntimeException(
          "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
    }

    order.setStatus(OrderStatus.CANCELLED);

    for (OrderItem item : order.getOrderItems()) {
      Product product = item.getProduct();
      product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
      productRepository.save(product);
    }

    Order cancelledOrder = orderRepository.save(order);
    emailService.sendOrderCancellationEmail(currentUser, cancelledOrder);

    return cancelledOrder;
  }

  // NEW: System-initiated cancellation for expired pending orders (used by Scheduled Tasks)
  @Transactional
  public Order cancelExpiredPendingOrder(Order order) {
    order.setStatus(OrderStatus.CANCELLED);

    for (OrderItem item : order.getOrderItems()) {
      Product product = item.getProduct();
      product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
      productRepository.save(product);
    }

    return orderRepository.save(order);
  }
} // <-- THIS CLOSING BRACE IS CRUCIAL
