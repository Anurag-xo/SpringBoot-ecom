package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DigitalDeliveryService {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  public Resource getDigitalDownloadResource(Long orderId, Long productId, User user) {
    // 1. Verify order exists and belongs to the requesting user
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You do not have permission to access this order");
    }

    // 2. Verify order is not cancelled
    if (order.getStatus() == OrderStatus.CANCELLED) {
      throw new RuntimeException(
          "This order has been cancelled and is no longer available for download");
    }

    // 3. Verify the order actually contains the requested product
    boolean hasProduct =
        order.getOrderItems().stream()
            .anyMatch(item -> item.getProduct().getId().equals(productId));

    if (!hasProduct) {
      throw new RuntimeException("This product was not part of this order");
    }

    // 4. Verify the product is flagged as digital
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    if (!Boolean.TRUE.equals(product.getIsDigital())) {
      throw new RuntimeException("This product is not a digital item");
    }

    if (product.getDigitalFilePath() == null || product.getDigitalFilePath().isEmpty()) {
      throw new RuntimeException("Digital file is missing for this product");
    }

    // 5. Resolve and return the file resource securely
    Path filePath = Paths.get(product.getDigitalFilePath()).normalize();
    Resource resource = new FileSystemResource(filePath);

    if (!resource.exists() || !resource.isReadable()) {
      throw new RuntimeException("Digital file not found on server");
    }

    return resource;
  }
}
