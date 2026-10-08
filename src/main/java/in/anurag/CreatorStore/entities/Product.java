package in.anurag.CreatorStore.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  private String category;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price; // Base price (can be overridden by variants)

  @Column(nullable = false)
  private Integer stockQuantity; // Total stock across all variants

  private String imageUrl;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ProductVariant> variants = new ArrayList<>();

  // Helper method to add a variant
  public void addVariant(ProductVariant variant) {
    variants.add(variant);
    variant.setProduct(this);
  }

  // Helper method to remove a variant
  public void removeVariant(ProductVariant variant) {
    variants.remove(variant);
    variant.setProduct(null);
  }
}
