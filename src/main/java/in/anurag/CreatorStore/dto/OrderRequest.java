package in.anurag.CreatorStore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
  @Valid
  @NotEmpty(message = "Order must contain at least one item")
  private List<OrderItemRequest> items;

  private String couponCode;

  // ✅ NEW: Address IDs for Checkout
  @NotNull(message = "Shipping address is required")
  private Long shippingAddressId;

  private Long billingAddressId; // Optional, defaults to shipping address if null
}
