package in.anurag.CreatorStore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OderRequest */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
  @NotBlank(message = "Customer name is required")
  private String customerName;

  @NotBlank(message = "Customer email is required")
  @Email(message = "Enter a valid email")
  private String customerEmail;

  @Valid
  @NotEmpty(message = "Order must contain at least one item")
  private List<OrderItemRequest> items;
}
