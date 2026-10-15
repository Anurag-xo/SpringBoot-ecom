package in.anurag.CreatorStore.dto;

import in.anurag.CreatorStore.entities.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CouponRequest {
  @NotBlank(message = "Coupon code is required")
  @Size(max = 50, message = "Code must be 50 characters or less")
  private String code;

  @NotNull(message = "Discount type is required")
  private DiscountType discountType;

  @NotNull(message = "Discount value is required")
  @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
  private BigDecimal discountValue;

  @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @DecimalMin(value = "0.0", message = "Max discount amount cannot be negative")
  private BigDecimal maxDiscountAmount;

  @NotNull(message = "Valid from date is required")
  private LocalDateTime validFrom;

  @NotNull(message = "Valid until date is required")
  private LocalDateTime validUntil;

  @Min(value = 1, message = "Usage limit must be at least 1")
  private Integer usageLimit;

  private Boolean isActive = true;
}
