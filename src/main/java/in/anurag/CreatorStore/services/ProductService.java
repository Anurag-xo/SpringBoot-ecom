package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.repositories.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public Product createProduct(Product product) {
    return productRepository.save(product);
  }

  public Product updateProduct(Long id, Product productDetails) {
    Product existingProduct =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    existingProduct.setName(productDetails.getName());
    existingProduct.setDescription(productDetails.getDescription());
    existingProduct.setCategory(productDetails.getCategory());
    existingProduct.setPrice(productDetails.getPrice());
    existingProduct.setStockQuantity(productDetails.getStockQuantity());

    return productRepository.save(existingProduct);
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product getProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
  }

  public void deleteProduct(Long id) {
    productRepository.deleteById(id);
  }
}
