package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.services.FileStorageService;
import in.anurag.CreatorStore.services.ProductService;
import in.anurag.CreatorStore.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
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
    description =
        "Product catalog management (CRUD, pagination, image uploads, and advanced search)")
public class ProductController {

  private final ProductService productService;
  private final FileStorageService fileStorageService;
  private final SearchService searchService;

  @Operation(
      summary = "Get all products",
      description =
          "Returns a paginated list of products. Supports SQL-based filtering by category and"
              + " search text.")
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
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @Operation(
      summary = "Create a product",
      description = "Admin only: Adds a new product to the catalog and indexes it in Meilisearch.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.createProduct(product));
  }

  @Operation(
      summary = "Update a product",
      description = "Admin only: Updates an existing product and syncs changes to Meilisearch.")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id, @Valid @RequestBody Product product) {
    return ResponseEntity.ok(productService.updateProduct(id, product));
  }

  @Operation(
      summary = "Delete a product",
      description =
          "Admin only: Removes a product from the catalog and deletes it from Meilisearch.")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Upload product image",
      description = "Admin only: Uploads an image for a specific product and updates the index.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/image")
  public ResponseEntity<Product> uploadProductImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    String fileName = fileStorageService.storeFile(file);
    String imageUrl = "/uploads/" + fileName;
    Product updatedProduct = productService.updateProductImage(id, imageUrl);

    return ResponseEntity.ok(updatedProduct);
  }

  // ==========================================
  // MEILISEARCH ENDPOINTS
  // ==========================================

  @Operation(
      summary = "Search products (Full-Text)",
      description = "Uses Meilisearch for typo-tolerant, relevance-ranked search.")
  @GetMapping("/search")
  public ResponseEntity<?> searchProducts(
      @RequestParam String q, @RequestParam(defaultValue = "20") int limit) {

    var results = searchService.searchProducts(q, limit);
    if (results == null) {
      return ResponseEntity.status(503).body("Search service is currently unavailable");
    }
    return ResponseEntity.ok(results);
  }

  @Operation(
      summary = "Advanced search with filters",
      description = "Full-text search with optional category and price range filters.")
  @GetMapping("/search/advanced")
  public ResponseEntity<?> advancedSearch(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(defaultValue = "20") int limit) {

    var results = searchService.searchProductsWithFilters(q, category, minPrice, maxPrice, limit);
    if (results == null) {
      return ResponseEntity.status(503).body("Search service is currently unavailable");
    }
    return ResponseEntity.ok(results);
  }

  @Operation(
      summary = "Reindex all products",
      description =
          "Admin only: Rebuilds the entire Meilisearch index from the database (useful after bulk"
              + " imports).")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/reindex")
  public ResponseEntity<String> reindexAllProducts() {
    List<Product> allProducts = productService.getAllProducts();
    searchService.reindexAllProducts(allProducts);
    return ResponseEntity.ok("Reindexing started for " + allProducts.size() + " products");
  }
}
