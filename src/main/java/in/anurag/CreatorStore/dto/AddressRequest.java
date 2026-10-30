package in.anurag.CreatorStore.dto;

import in.anurag.CreatorStore.entities.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequest {
  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Address line 1 is required")
  private String addressLine1;

  private String addressLine2;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "State is required")
  private String state;

  @NotBlank(message = "Postal code is required")
  private String postalCode;

  @NotBlank(message = "Country is required")
  private String country;

  private String phoneNumber;

  @NotNull(message = "Address type is required")
  private AddressType addressType;

  private Boolean isDefault = false;
}
