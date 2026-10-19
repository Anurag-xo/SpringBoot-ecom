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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final SearchService searchService;

  @Transactional
  public Product createProduct(Product product) {
    Product savedProduct = productRepository.save(product);
    searchService.indexProduct(savedProduct);
    return savedProduct;
  }

  @Transactional
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
    Product savedProduct = productRepository.save(existingProduct);
    searchService.indexProduct(savedProduct);
    return savedProduct;
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product getProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  @Transactional
  public void deleteProduct(Long id) {
    // ✅ UPDATED: Verify it exists first (findById respects @SQLRestriction)
    productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

    // ✅ UPDATED: Soft delete instead of hard delete
    productRepository.softDeleteById(id);
    searchService.deleteProductFromIndex(id);
  }

  @Transactional
  public Product updateProductImage(Long id, String imageUrl) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    product.setImageUrl(imageUrl);
    Product savedProduct = productRepository.save(product);
    searchService.indexProduct(savedProduct);
    return savedProduct;
  }

  public Page<Product> getAllProducts(
      int page, int size, String sortBy, String sortDir, String category, String search) {
    Sort sort =
        sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);

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
      return productRepository.findAll(pageable);
    }
  }
}
