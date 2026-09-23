package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.ReviewRequest;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.Review;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;

  @Transactional
  public Review addReview(Long productId, ReviewRequest request, User user) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    // Business Logic: Prevent duplicate reviews
    if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
      throw new RuntimeException("You have already reviewed this product");
    }

    Review review =
        Review.builder()
            .rating(request.getRating())
            .comment(request.getComment())
            .product(product)
            .user(user)
            .build();

    return reviewRepository.save(review);
  }

  public List<Review> getReviewsByProduct(Long productId) {
    if (!productRepository.existsById(productId)) {
      throw new ResourceNotFoundException("Product not found with id: " + productId);
    }
    return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
  }

  public Double getAverageRating(Long productId) {
    Double avg = reviewRepository.getAverageRatingByProductId(productId);
    return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0; // Rounds to 1 decimal place
  }
}
