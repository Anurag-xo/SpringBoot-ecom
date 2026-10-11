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

  public List<Review> getReviewsByProductId(Long productId) {
    return reviewRepository.findByProductId(productId);
  }

  @Transactional
  public Review createReview(Long productId, ReviewRequest request, User user) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));

    Review review =
        Review.builder()
            .product(product)
            .user(user)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();

    return reviewRepository.save(review);
  }

  @Transactional
  public void deleteReview(Long reviewId, User user) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Review not found with id: " + reviewId));

    if (!review.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You do not have permission to delete this review");
    }

    reviewRepository.delete(review);
  }
}
