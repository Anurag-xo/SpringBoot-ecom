package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.services.FileStorageService;
import in.anurag.CreatorStore.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Products",
    description = "Product catalog management (CRUD, pagination, and image uploads)")
public class ProductController {

  private final ProductService productService;
  private final FileStorageService fileStorageService;

  @Operation(
      summary = "Get all products",
      description =
          "Returns a paginated list of products. Supports filtering by category and search text.")
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

  @Operation(summary = "Get product by ID", description = "Returns a single product by its ID.")
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @Operation(
      summary = "Create a product",
      description = "Admin only: Adds a new product to the catalog.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.createProduct(product));
  }

  @Operation(summary = "Update a product", description = "Admin only: Updates an existing product.")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id, @Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.updateProduct(id, product));
  }

  @Operation(
      summary = "Delete a product",
      description = "Admin only: Removes a product from the catalog.")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Upload product image",
      description = "Admin only: Uploads an image for a specific product.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/image")
  public ResponseEntity<Product> uploadProductImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    String fileName = fileStorageService.storeFile(file);
    String imageUrl = "/uploads/" + fileName;
    Product updatedProduct = productService.updateProductImage(id, imageUrl);

    return ResponseEntity.ok(updatedProduct);
  }
}
