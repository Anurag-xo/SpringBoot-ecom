package in.anurag.CreatorStore.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
  private String productName;
  private Long totalQuantitySold;
  private BigDecimal totalRevenueGenerated;
}
