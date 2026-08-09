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

  public Product updateProduct(Long id, Product product) {
    Product existingProduct =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    existingProduct.setName(product.getName());
    existingProduct.setDescription(product.getDescription());
    existingProduct.setCategory(product.getCategory());
    existingProduct.setPrice(product.getPrice());
    existingProduct.setStockQuantity(product.getStockQuantity());

    return productRepository.save(existingProduct);
  }

  public List<Product> getProducts() {
    return productRepository.findAll();
  }

  public Product getProductsById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
  }

  public void deleteProduct(Long id) {
    productRepository.deleteById(id);
  }
}
