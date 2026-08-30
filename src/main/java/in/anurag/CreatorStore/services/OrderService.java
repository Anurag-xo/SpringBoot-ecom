package in.anurag.CreatorStore.services;

// Consider renaming to OrderRequest (see section 3)
import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderItem;
import in.anurag.CreatorStore.entities.Product;
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

    order.setCustomerName(orderRequest.getCustomerName()); // Fixed typo
    order.setCustomerEmail(orderRequest.getCustomerEmail());
    order.setStatus("CONFIRMED");

    for (OrderItemRequest itemRequest : orderRequest.getItems()) {
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Product not found with id: " + itemRequest.getProductId()));

      if (product.getStockQuantity() < itemRequest.getQuantity()) {
        throw new RuntimeException(
            "Not enough stock for product id: " + itemRequest.getProductId());
      }

      BigDecimal priceOfItem =
          product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      totalPrice = totalPrice.add(priceOfItem);

      // Fixed syntax error (removed extra ");")
      product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

      // Fixed: use the injected instance 'productRepository', not the class name
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

    // Fixed: Actually save the order to the database before returning
    return orderRepository.save(order);
  }
}
