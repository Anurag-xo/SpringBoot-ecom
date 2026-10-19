package in.anurag.CreatorStore.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
@SQLRestriction("deleted_at IS NULL") // ✅ NEW: Automatically filters out soft-deleted products
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
  private BigDecimal price;

  @Column(nullable = false)
  private Integer stockQuantity;

  private String imageUrl;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @NotAudited
  private List<ProductVariant> variants = new ArrayList<>();

  // ✅ NEW: Soft delete timestamp
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void addVariant(ProductVariant variant) {
    variants.add(variant);
    variant.setProduct(this);
  }

  public void removeVariant(ProductVariant variant) {
    variants.remove(variant);
    variant.setProduct(null);
  }
}
