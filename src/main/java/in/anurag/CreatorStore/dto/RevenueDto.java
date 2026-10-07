package in.anurag.CreatorStore.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDto {
  private BigDecimal totalRevenue;
  private Long totalOrders;
  private Integer periodDays;
}
