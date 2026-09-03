package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.services.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  // Public endpoints - anyone can view products
  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    return ResponseEntity.ok(productService.getAllProducts());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  // Admin-only endpoints - require ADMIN role
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.createProduct(product));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id, @Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.updateProduct(id, product));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}
