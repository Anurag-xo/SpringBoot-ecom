package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.VariantRequest;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.ProductVariant;
import in.anurag.CreatorStore.services.FileStorageService;
import in.anurag.CreatorStore.services.ProductService;
import in.anurag.CreatorStore.services.ProductVariantService;
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
@RequestMapping("/api/v1/products") // ✅ UPDATED: Added v1 versioning
@RequiredArgsConstructor
@Tag(
    name = "Products",
    description =
        "Product catalog management (CRUD, pagination, image uploads, and advanced search)")
public class ProductController {

  private final ProductService productService;
  private final ProductVariantService productVariantService;
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
    return ResponseEntity.ok(
        productService.getAllProducts(page, size, sortBy, sortDir, category, search));
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
    return ResponseEntity.ok(productService.updateProductImage(id, imageUrl));
  }

  // ==========================================
  // PRODUCT VARIANT ENDPOINTS
  // ==========================================

  @Operation(
      summary = "Get all variants for a product",
      description = "Returns all variants (including inactive) for a specific product.")
  @GetMapping("/{productId}/variants")
  public ResponseEntity<List<ProductVariant>> getProductVariants(@PathVariable Long productId) {
    return ResponseEntity.ok(productVariantService.getVariantsByProductId(productId));
  }

  @Operation(
      summary = "Get active variants for a product",
      description = "Returns only active variants for a specific product.")
  @GetMapping("/{productId}/variants/active")
  public ResponseEntity<List<ProductVariant>> getActiveProductVariants(
      @PathVariable Long productId) {
    return ResponseEntity.ok(productVariantService.getActiveVariantsByProductId(productId));
  }

  @Operation(
      summary = "Create a product variant",
      description = "Admin only: Adds a new variant (size/color) to a product.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{productId}/variants")
  public ResponseEntity<ProductVariant> createVariant(
      @PathVariable Long productId, @Valid @RequestBody VariantRequest request) {
    return ResponseEntity.ok(productVariantService.createVariant(productId, request));
  }

  @Operation(
      summary = "Update a product variant",
      description = "Admin only: Updates an existing variant.")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/variants/{variantId}")
  public ResponseEntity<ProductVariant> updateVariant(
      @PathVariable Long variantId, @Valid @RequestBody VariantRequest request) {
    return ResponseEntity.ok(productVariantService.updateVariant(variantId, request));
  }

  @Operation(
      summary = "Delete a product variant",
      description = "Admin only: Permanently removes a variant.")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/variants/{variantId}")
  public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
    productVariantService.deleteVariant(variantId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Toggle variant active status",
      description = "Admin only: Activates or deactivates a variant without deleting it.")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/variants/{variantId}/toggle")
  public ResponseEntity<Void> toggleVariantActive(@PathVariable Long variantId) {
    productVariantService.toggleVariantActive(variantId);
    return ResponseEntity.ok().build();
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
      description = "Admin only: Rebuilds the entire Meilisearch index from the database.")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/reindex")
  public ResponseEntity<String> reindexAllProducts() {
    List<Product> allProducts = productService.getAllProducts();
    searchService.reindexAllProducts(allProducts);
    return ResponseEntity.ok("Reindexing started for " + allProducts.size() + " products");
  }
}
