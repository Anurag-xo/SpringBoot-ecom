package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.ReviewRequest;
import in.anurag.CreatorStore.entities.Review;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.ReviewService;
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
@RequestMapping("/api/v1/reviews") // ✅ UPDATED: Added v1 versioning
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review management endpoints")
public class ReviewController {

  private final ReviewService reviewService;
  private final UserRepository userRepository;

  @Operation(summary = "Get all reviews for a product")
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
    return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
  }

  @Operation(summary = "Create a review for a product")
  @PostMapping("/product/{productId}")
  public ResponseEntity<Review> createReview(
      @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
    return ResponseEntity.ok(reviewService.createReview(productId, request, getCurrentUser()));
  }

  @Operation(summary = "Delete a review")
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
    reviewService.deleteReview(reviewId, getCurrentUser());
    return ResponseEntity.noContent().build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
