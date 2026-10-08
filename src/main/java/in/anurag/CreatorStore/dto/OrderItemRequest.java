package in.anurag.CreatorStore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  // NEW: Optional variant ID for products with variants
  private Long variantId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;
}
