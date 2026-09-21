package in.anurag.CreatorStore.dto;

import in.anurag.CreatorStore.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
  @NotNull(message = "Order status is required")
  private OrderStatus status;
}
