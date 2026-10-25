package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.Coupon;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.ProductVariant;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Audit", description = "Historical change tracking and compliance endpoints")
public class AuditController {
  private final AuditService auditService;

  @Operation(summary = "Get Product Change History")
  @GetMapping("/products/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getProductHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(Product.class, id));
  }

  @Operation(summary = "Get Order Change History")
  @GetMapping("/orders/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getOrderHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(Order.class, id));
  }

  @Operation(summary = "Get Coupon Change History")
  @GetMapping("/coupons/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getCouponHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(Coupon.class, id));
  }

  @Operation(summary = "Get User Profile Change History")
  @GetMapping("/users/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getUserHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(User.class, id));
  }

  // ✅ NEW: Product Variant Audit
  @Operation(summary = "Get Product Variant Change History")
  @GetMapping("/variants/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getVariantHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(ProductVariant.class, id));
  }
}
