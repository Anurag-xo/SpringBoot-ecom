package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.VariantRequest;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.ProductVariant;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ProductVariantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

  private final ProductVariantRepository variantRepository;
  private final ProductRepository productRepository;
  private final SearchService searchService;

  @Transactional
  public ProductVariant createVariant(Long productId, VariantRequest request) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    if (variantRepository.existsBySku(request.getSku())) {
      throw new RuntimeException("Variant with SKU " + request.getSku() + " already exists");
    }

    ProductVariant variant =
        ProductVariant.builder()
            .product(product)
            .sku(request.getSku())
            .size(request.getSize())
            .color(request.getColor())
            .material(request.getMaterial())
            .stockQuantity(request.getStockQuantity())
            .price(request.getPrice())
            .imageUrl(request.getImageUrl())
            .active(request.getActive())
            .build();

    ProductVariant savedVariant = variantRepository.save(variant);

    // Update product's total stock
    updateProductTotalStock(product);

    // Re-index product in Meilisearch
    searchService.indexProduct(product);

    return savedVariant;
  }

  public List<ProductVariant> getVariantsByProductId(Long productId) {
    return variantRepository.findByProductId(productId);
  }

  public List<ProductVariant> getActiveVariantsByProductId(Long productId) {
    return variantRepository.findByProductIdAndActiveTrue(productId);
  }

  public ProductVariant getVariantById(Long variantId) {
    return variantRepository
        .findById(variantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Variant not found with id: " + variantId));
  }

  public ProductVariant getVariantBySku(String sku) {
    return variantRepository
        .findBySku(sku)
        .orElseThrow(() -> new ResourceNotFoundException("Variant not found with SKU: " + sku));
  }

  @Transactional
  public ProductVariant updateVariant(Long variantId, VariantRequest request) {
    ProductVariant variant =
        variantRepository
            .findById(variantId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Variant not found with id: " + variantId));

    // Check if SKU is being changed and if new SKU already exists
    if (!variant.getSku().equals(request.getSku())
        && variantRepository.existsBySku(request.getSku())) {
      throw new RuntimeException("Variant with SKU " + request.getSku() + " already exists");
    }

    variant.setSku(request.getSku());
    variant.setSize(request.getSize());
    variant.setColor(request.getColor());
    variant.setMaterial(request.getMaterial());
    variant.setStockQuantity(request.getStockQuantity());
    variant.setPrice(request.getPrice());
    variant.setImageUrl(request.getImageUrl());
    variant.setActive(request.getActive());

    ProductVariant savedVariant = variantRepository.save(variant);

    // Update product's total stock
    updateProductTotalStock(variant.getProduct());

    // Re-index product in Meilisearch
    searchService.indexProduct(variant.getProduct());

    return savedVariant;
  }

  @Transactional
  public void deleteVariant(Long variantId) {
    ProductVariant variant =
        variantRepository
            .findById(variantId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Variant not found with id: " + variantId));

    Product product = variant.getProduct();
    variantRepository.delete(variant);

    // Update product's total stock
    updateProductTotalStock(product);

    // Re-index product in Meilisearch
    searchService.indexProduct(product);
  }

  @Transactional
  public void toggleVariantActive(Long variantId) {
    ProductVariant variant =
        variantRepository
            .findById(variantId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Variant not found with id: " + variantId));

    variant.setActive(!variant.getActive());
    variantRepository.save(variant);

    // Re-index product in Meilisearch
    searchService.indexProduct(variant.getProduct());
  }

  // Helper method to recalculate total product stock from all variants
  private void updateProductTotalStock(Product product) {
    List<ProductVariant> variants = variantRepository.findByProductIdAndActiveTrue(product.getId());
    int totalStock = variants.stream().mapToInt(ProductVariant::getStockQuantity).sum();

    product.setStockQuantity(totalStock);
    productRepository.save(product);
  }
}
