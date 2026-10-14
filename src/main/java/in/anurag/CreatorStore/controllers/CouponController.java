package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.CouponRequest;
import in.anurag.CreatorStore.dto.CouponValidationResponse;
import in.anurag.CreatorStore.entities.Coupon;
import in.anurag.CreatorStore.services.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon management and validation endpoints")
public class CouponController {

  private final CouponService couponService;

  @Operation(summary = "Validate a coupon code (Public)")
  @PostMapping("/coupons/validate")
  public ResponseEntity<CouponValidationResponse> validateCoupon(
      @RequestParam String code, @RequestParam BigDecimal cartTotal) {
    return ResponseEntity.ok(couponService.validateCoupon(code, cartTotal));
  }

  @Operation(summary = "Create a new coupon (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin/coupons")
  public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody CouponRequest request) {
    return ResponseEntity.ok(couponService.createCoupon(request));
  }

  @Operation(summary = "Get all coupons (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/coupons")
  public ResponseEntity<List<Coupon>> getAllCoupons() {
    return ResponseEntity.ok(couponService.getAllCoupons());
  }

  @Operation(summary = "Update a coupon (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/admin/coupons/{id}")
  public ResponseEntity<Coupon> updateCoupon(
      @PathVariable Long id, @Valid @RequestBody CouponRequest request) {
    return ResponseEntity.ok(couponService.updateCoupon(id, request));
  }

  @Operation(summary = "Delete a coupon (Admin only)")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/admin/coupons/{id}")
  public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
    couponService.deleteCoupon(id);
    return ResponseEntity.noContent().build();
  }
}
