package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.CouponValidationResponse;
import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderItem;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.ProductVariant;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.UserAddress;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ProductVariantRepository;
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
  private final ProductVariantRepository variantRepository;
  private final EmailService emailService;
  private final CouponService couponService; // ✅ ADDED for coupon processing
  private final AddressService addressService; // ✅ NEW
  private final ShippingService shippingService; // ✅ NEW

  @Transactional
  public Order createOrder(OrderRequest orderRequest, User user) {
    // ✅ NEW: Fetch and validate addresses
    UserAddress shippingAddress =
        addressService.getAddressById(orderRequest.getShippingAddressId(), user);
    UserAddress billingAddress =
        orderRequest.getBillingAddressId() != null
            ? addressService.getAddressById(orderRequest.getBillingAddressId(), user)
            : shippingAddress;

    Order order = new Order();
    order.setCustomerName(shippingAddress.getFullName()); // ✅ UPDATED: Derived from address
    order.setCustomerEmail(user.getEmail()); // ✅ UPDATED: Derived from user
    order.setStatus(OrderStatus.PENDING);
    order.setUser(user);
    order.setShippingAddress(shippingAddress); // ✅ NEW
    order.setBillingAddress(billingAddress); // ✅ NEW

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;

    for (OrderItemRequest itemRequest : orderRequest.getItems()) {
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found with id: " + itemRequest.getProductId()));

      ProductVariant variant = null;
      BigDecimal price = product.getPrice();

      if (itemRequest.getVariantId() != null) {
        variant =
            variantRepository
                .findById(itemRequest.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        price = variant.getPrice();
        if (variant.getStockQuantity() < itemRequest.getQuantity()) {
          throw new RuntimeException("Not enough stock for variant: " + variant.getSku());
        }
        variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
        variantRepository.save(variant);
      } else {
        if (product.getStockQuantity() < itemRequest.getQuantity()) {
          throw new RuntimeException("Not enough stock for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
        productRepository.save(product);
      }

      OrderItem orderItem =
          OrderItem.builder()
              .order(order)
              .product(product)
              .variant(variant)
              .quantity(itemRequest.getQuantity())
              .priceAtPurchase(price)
              .build();
      orderItems.add(orderItem);
      subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
    }

    // ✅ NEW: Calculate shipping
    BigDecimal shippingCost = shippingService.calculateShippingCost(subtotal);
    order.setShippingCost(shippingCost);

    // ✅ NEW: Process Coupon
    BigDecimal discountAmount = BigDecimal.ZERO;
    if (orderRequest.getCouponCode() != null && !orderRequest.getCouponCode().isBlank()) {
      CouponValidationResponse validation =
          couponService.validateCoupon(orderRequest.getCouponCode(), subtotal);
      if (validation.isValid()) {
        discountAmount = validation.getDiscountAmount();
        order.setCouponCode(orderRequest.getCouponCode());
      }
    }
    order.setDiscountAmount(discountAmount);

    // ✅ UPDATED: Total price now includes shipping
    order.setTotalPrice(subtotal.add(shippingCost).subtract(discountAmount));
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

  @Transactional
  public void deleteOrder(Long orderId) {
    orderRepository
        .findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    orderRepository.softDeleteById(orderId);
  }
}
