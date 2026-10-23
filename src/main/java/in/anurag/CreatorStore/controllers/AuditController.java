package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.entities.*;
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

  // ✅ NEW: Order Audit
  @Operation(summary = "Get Order Change History")
  @GetMapping("/orders/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getOrderHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(Order.class, id));
  }

  // ✅ NEW: Coupon Audit
  @Operation(summary = "Get Coupon Change History")
  @GetMapping("/coupons/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getCouponHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(Coupon.class, id));
  }

  // ✅ NEW: User Audit
  @Operation(summary = "Get User Profile Change History")
  @GetMapping("/users/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getUserHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getEntityHistory(User.class, id));
  }
}
