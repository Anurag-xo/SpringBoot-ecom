package in.anurag.CreatorStore.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
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
  @NotAudited // <-- Tells Envers to ignore this relationship
  private List<ProductVariant> variants = new ArrayList<>();

  public void addVariant(ProductVariant variant) {
    variants.add(variant);
    variant.setProduct(this);
  }

  public void removeVariant(ProductVariant variant) {
    variants.remove(variant);
    variant.setProduct(null);
  }
}
