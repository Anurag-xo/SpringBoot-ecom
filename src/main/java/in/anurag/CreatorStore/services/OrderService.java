package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

/** OrderService This is wehre out entire applicatoin comes together. */
@Service
@RequiredArgsConstructor
public class OrderService {
  private OrderRepository orderRepository;
  private ProductRepository productRepository;

  // method that will help us to create orders
  @Transactional
  public Order createOrder(orderRequest orderRequest) {
    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalPrice = BigDecimal.ZERO;
    Order order = new Order();
    order.setCustomreName(orderRequest.getCustomerName());
    order.setCustomerEmail(orderRequest.getCustomerEmail());
    order.setStatus("CONFIRMED");

    for (OrderItemRequest itemRequest : orderRequest.getItems()) {
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Product not found with id" + itemRequest.getProductId()));

      // check the product stock
      if (product.getStockQuantity() < itemRequest.getQuantity) {
        throw new RuntimeException("Not enough stock for " + itemRequest.getProductId());
      }
      
      // Calculate total price
      BigDecimal priceOfItem = product.getPrice()
        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

      totalPrice = totalPrice.add(priceOfItem);

      // Update the product table with latest stock quantity
      product.setStockQuantity(
          product.getStockQuantity() - itemRequest.getQuantity());
      );
      ProductRepository.save(product);

      // Builder pattern to make object
      OrderItem orderItem = OrderItem.builder()
        .order(order)
        .product(product)
        .quantity(itemRequest.getQuantity())
        .priceAtPurchase(product.getPrice())
        .build();

      orderItems.add(orderItem);

    }

    order.setTotalPrice(totalPrice);
    order.setOrderItems(orderItems);

    return order;
  }
}
