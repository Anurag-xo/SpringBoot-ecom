package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

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
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  public void deleteProduct(Long id) {
    productRepository.deleteById(id);
  }

  // NEW: Paginated, sorted, and filtered product retrieval
  public Page<Product> getAllProducts(
      int page, int size, String sortBy, String sortDir, String category, String search) {

    // 1. Determine sort direction
    Sort sort =
        sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

    // 2. Create Pageable object
    Pageable pageable = PageRequest.of(page, size, sort);

    // 3. Apply filters dynamically
    boolean hasCategory = category != null && !category.trim().isEmpty();
    boolean hasSearch = search != null && !search.trim().isEmpty();

    if (hasCategory && hasSearch) {
      return productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(
          category, search, pageable);
    } else if (hasCategory) {
      return productRepository.findByCategoryIgnoreCase(category, pageable);
    } else if (hasSearch) {
      return productRepository.findByNameContainingIgnoreCase(search, pageable);
    } else {
      // No filters applied, return all products paginated
      return productRepository.findAll(pageable);
    }
  }
}
