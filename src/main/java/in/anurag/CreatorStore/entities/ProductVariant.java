package in.anurag.CreatorStore.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.Builder;

@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "sku"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(nullable = false)
  private String size;

  @Column(nullable = false)
  private String color;

  private String material;

  @Column(nullable = false)
  private Integer stockQuantity;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  private String imageUrl;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;
}
