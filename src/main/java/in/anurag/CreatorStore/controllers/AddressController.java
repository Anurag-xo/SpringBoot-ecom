package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.AddressRequest;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.UserAddress;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "User address management endpoints")
public class AddressController {
  private final AddressService addressService;
  private final UserRepository userRepository;

  @Operation(summary = "Get all addresses for current user")
  @GetMapping
  public ResponseEntity<List<UserAddress>> getMyAddresses() {
    return ResponseEntity.ok(addressService.getAddressesByUser(getCurrentUser()));
  }

  @Operation(summary = "Get address by ID")
  @GetMapping("/{id}")
  public ResponseEntity<UserAddress> getAddressById(@PathVariable Long id) {
    return ResponseEntity.ok(addressService.getAddressById(id, getCurrentUser()));
  }

  @Operation(summary = "Create a new address")
  @PostMapping
  public ResponseEntity<UserAddress> createAddress(@Valid @RequestBody AddressRequest request) {
    return ResponseEntity.ok(addressService.createAddress(request, getCurrentUser()));
  }

  @Operation(summary = "Update an existing address")
  @PutMapping("/{id}")
  public ResponseEntity<UserAddress> updateAddress(
      @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
    return ResponseEntity.ok(addressService.updateAddress(id, request, getCurrentUser()));
  }

  @Operation(summary = "Delete an address")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
    addressService.deleteAddress(id, getCurrentUser());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Set an address as default")
  @PatchMapping("/{id}/default")
  public ResponseEntity<UserAddress> setDefaultAddress(@PathVariable Long id) {
    return ResponseEntity.ok(addressService.setDefaultAddress(id, getCurrentUser()));
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
