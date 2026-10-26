package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.ProductRequest;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.events.ProductUpdatedEvent;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

  private final ProductRepository productRepository;

  // ✅ NEW: Inject Event Publisher
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public Product createProduct(ProductRequest request) {
    Product product =
        Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .category(request.getCategory())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .imageUrl(request.getImageUrl())
            .isDigital(request.getIsDigital() != null && request.getIsDigital())
            .build();

    Product savedProduct = productRepository.save(product);

    // ✅ NEW: Publish Event (Replaces direct searchService call)
    eventPublisher.publishEvent(new ProductUpdatedEvent(this, savedProduct));
    log.info("🚀 Published ProductUpdatedEvent for new Product #{}", savedProduct.getId());

    return savedProduct;
  }

  @Transactional
  public Product updateProduct(Long id, ProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setCategory(request.getCategory());
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setImageUrl(request.getImageUrl());

    Product updatedProduct = productRepository.save(product);

    // ✅ NEW: Publish Event
    eventPublisher.publishEvent(new ProductUpdatedEvent(this, updatedProduct));
    log.info("🚀 Published ProductUpdatedEvent for updated Product #{}", updatedProduct.getId());

    return updatedProduct;
  }

  public Page<Product> getAllProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  public Product getProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  @Transactional
  public void deleteProduct(Long id) {
    Product product = getProductById(id);
    product.setDeletedAt(LocalDateTime.now());
    productRepository.save(product);
  }

  // ... Keep your existing digital file update methods here ...
}
