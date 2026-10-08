package in.anurag.CreatorStore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VariantRequest {

  @NotBlank(message = "SKU is required")
  private String sku;

  @NotBlank(message = "Size is required")
  private String size;

  @NotBlank(message = "Color is required")
  private String color;

  private String material;

  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock quantity cannot be negative")
  private Integer stockQuantity;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", message = "Price cannot be negative")
  private BigDecimal price;

  private String imageUrl;

  private Boolean active = true;
}
