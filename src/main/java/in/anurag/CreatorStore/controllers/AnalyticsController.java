package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.RevenueDto;
import in.anurag.CreatorStore.dto.TopProductDto;
import in.anurag.CreatorStore.dto.UserGrowthDto;
import in.anurag.CreatorStore.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/analytics") // ✅ UPDATED: Added v1 versioning
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
    name = "Admin Analytics",
    description = "Business intelligence and reporting endpoints for administrators")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @Operation(
      summary = "Get Revenue Analytics",
      description = "Returns total revenue and order count for the specified period in days.")
  @GetMapping("/revenue")
  public ResponseEntity<RevenueDto> getRevenueAnalytics(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(analyticsService.getRevenueAnalytics(days));
  }

  @Operation(
      summary = "Get Top Selling Products",
      description = "Returns the best-selling products for the specified period.")
  @GetMapping("/top-products")
  public ResponseEntity<List<TopProductDto>> getTopSellingProducts(
      @RequestParam(defaultValue = "30") int days, @RequestParam(defaultValue = "5") int limit) {
    return ResponseEntity.ok(analyticsService.getTopSellingProducts(days, limit));
  }

  @Operation(
      summary = "Get User Growth",
      description = "Returns daily new user registrations for the specified period.")
  @GetMapping("/user-growth")
  public ResponseEntity<List<UserGrowthDto>> getUserGrowth(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(analyticsService.getUserGrowth(days));
  }
}
