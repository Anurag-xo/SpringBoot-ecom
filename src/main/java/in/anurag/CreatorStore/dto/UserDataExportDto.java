package in.anurag.CreatorStore.dto;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Review;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.Wishlist;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataExportDto {
  private User user;
  private List<Order> orders;
  private List<Review> reviews;
  private List<Wishlist> wishlists;
}
