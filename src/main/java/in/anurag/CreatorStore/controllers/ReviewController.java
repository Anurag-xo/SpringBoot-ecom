package in.anurag.CreatorStore.controllers;

import in.anurag.CreatorStore.dto.ReviewRequest;
import in.anurag.CreatorStore.entities.Review;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.services.ReviewService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;
  private final UserRepository userRepository;

  // 1. Add a review (Authenticated users only)
  @PostMapping
  public ResponseEntity<Review> addReview(
      @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {

    User currentUser = getCurrentUser();
    return ResponseEntity.ok(reviewService.addReview(productId, request, currentUser));
  }

  // 2. Get all reviews and average rating for a product (Public)
  @GetMapping
  public ResponseEntity<Map<String, Object>> getProductReviews(@PathVariable Long productId) {
    List<Review> reviews = reviewService.getReviewsByProduct(productId);
    Double averageRating = reviewService.getAverageRating(productId);

    Map<String, Object> response = new HashMap<>();
    response.put("averageRating", averageRating);
    response.put("totalReviews", reviews.size());
    response.put("reviews", reviews);

    return ResponseEntity.ok(response);
  }

  // Helper method to extract the logged-in user
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }
}
