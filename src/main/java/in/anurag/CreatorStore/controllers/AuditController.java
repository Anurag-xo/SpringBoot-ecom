package in.anurag.CreatorStore.controllers;

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
@PreAuthorize("hasRole('ADMIN')") // Lock down to admins only
@Tag(name = "Admin Audit", description = "Historical change tracking and compliance endpoints")
public class AuditController {

  private final AuditService auditService;

  @Operation(
      summary = "Get Product Change History",
      description =
          "Returns the complete revision history of a specific product, including who changed it"
              + " and when.")
  @GetMapping("/products/{id}/history")
  public ResponseEntity<List<Map<String, Object>>> getProductHistory(@PathVariable Long id) {
    return ResponseEntity.ok(auditService.getProductHistory(id));
  }
}
