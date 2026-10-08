package in.anurag.CreatorStore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  @JsonIgnore
  private Cart cart;

  // EAGER fetch so product details (name, image, current stock) are returned in the cart JSON
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private Integer quantity;

  // Snapshot of price at the time of adding to cart
  @Column(nullable = false)
  private BigDecimal price;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;
}
