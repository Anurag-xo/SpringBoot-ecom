package in.anurag.CreatorStore.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {
  private boolean isValid;
  private BigDecimal discountAmount;
  private String message;
}
