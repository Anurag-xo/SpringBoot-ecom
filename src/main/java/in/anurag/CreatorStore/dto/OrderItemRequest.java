package in.anurag.CreatorStore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OrderItemRequest */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

  @NotNull(message = "Product Id is required")
  private Long productId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be atleast 1")
  private Integer quantity;
}
