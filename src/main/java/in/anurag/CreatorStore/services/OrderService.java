package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.*;
import in.anurag.CreatorStore.events.LowStockEvent;
import in.anurag.CreatorStore.events.OrderCreatedEvent;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CouponService couponService;

  // ✅ NEW: Inject Event Publisher
  private final ApplicationEventPublisher eventPublisher;

  private static final int LOW_STOCK_THRESHOLD = 5;

  @Transactional
  public Order createOrder(OrderRequest request, User user) {
    Order order =
        Order.builder()
            .customerName(request.getCustomerName())
            .customerEmail(request.getCustomerEmail())
            .user(user)
            .orderItems(new ArrayList<>())
            .build();

    BigDecimal totalPrice = BigDecimal.ZERO;

    for (OrderItemRequest itemReq : request.getItems()) {
      Product product =
          productRepository
              .findById(itemReq.getProductId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found with id: " + itemReq.getProductId()));

      ProductVariant variant = null;
      int currentStock = product.getStockQuantity();

      if (itemReq.getVariantId() != null) {
        variant =
            productVariantRepository
                .findById(itemReq.getVariantId())
                .orElseThrow(
                    () ->
                        new ResourceNotFoundException(
                            "Variant not found with id: " + itemReq.getVariantId()));
        currentStock = variant.getStockQuantity();
      }

      if (currentStock < itemReq.getQuantity()) {
        throw new IllegalStateException("Insufficient stock for product: " + product.getName());
      }

      BigDecimal itemPrice = variant != null ? variant.getPrice() : product.getPrice();
      BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      totalPrice = totalPrice.add(itemTotal);

      OrderItem orderItem =
          OrderItem.builder()
              .order(order)
              .product(product)
              .variant(variant)
              .quantity(itemReq.getQuantity())
              .priceAtPurchase(itemPrice)
              .build();

      order.getOrderItems().add(orderItem);

      // Decrement Stock & Check Thresholds
      if (variant != null) {
        variant.setStockQuantity(currentStock - itemReq.getQuantity());
        productVariantRepository.save(variant);

        if (variant.getStockQuantity() <= LOW_STOCK_THRESHOLD && variant.getStockQuantity() > 0) {
          eventPublisher.publishEvent(new LowStockEvent(this, product, variant.getStockQuantity()));
        }
      } else {
        product.setStockQuantity(currentStock - itemReq.getQuantity());
        productRepository.save(product);

        if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD && product.getStockQuantity() > 0) {
          eventPublisher.publishEvent(new LowStockEvent(this, product, product.getStockQuantity()));
        }
      }
    }

    // Handle Coupon
    BigDecimal discountAmount = BigDecimal.ZERO;
    if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
      Coupon coupon = couponService.validateAndApplyCoupon(request.getCouponCode(), totalPrice);
      order.setCouponCode(coupon.getCode());
      discountAmount = couponService.calculateDiscount(coupon, totalPrice);
      order.setDiscountAmount(discountAmount);
    }

    order.setTotalPrice(totalPrice.subtract(discountAmount));
    Order savedOrder = orderRepository.save(order);

    // ✅ NEW: Publish Event (Replaces direct emailService call)
    eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));
    log.info("🚀 Published OrderCreatedEvent for Order #{}", savedOrder.getId());

    return savedOrder;
  }

  // ... Keep your existing getOrders, updateStatus, etc. methods here ...
}
