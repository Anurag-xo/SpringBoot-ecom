package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.services.FileStorageService;
import in.anurag.CreatorStore.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final FileStorageService fileStorageService;

  @GetMapping
  public ResponseEntity<Page<Product>> getAllProducts(
      @RequestParam(defaultValue = "0")
          @Min(value = 0, message = "Page number must be 0 or greater")
          int page,
      @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1")
          int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String search) {

    Page<Product> products =
        productService.getAllProducts(page, size, sortBy, sortDir, category, search);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

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

  // Upload Image Endpoint
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/image")
  public ResponseEntity<Product> uploadProductImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    // 1. Save the file and get the unique filename
    String fileName = fileStorageService.storeFile(file);

    // 2. Create the URL path that the frontend will use to access the image
    String imageUrl = "/uploads/" + fileName;

    // 3. Update the product in the database with the new image URL
    Product updatedProduct = productService.updateProductImage(id, imageUrl);

    return ResponseEntity.ok(updatedProduct);
  }
}
