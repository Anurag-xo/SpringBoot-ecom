package in.anurag.CreatorStore.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "coupons", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiscountType discountType;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "min_order_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @Column(name = "max_discount_amount", precision = 10, scale = 2)
  private BigDecimal maxDiscountAmount;

  @Column(name = "valid_from", nullable = false)
  private LocalDateTime validFrom;

  @Column(name = "valid_until", nullable = false)
  private LocalDateTime validUntil;

  @Column(name = "usage_limit")
  private Integer usageLimit; // Null means unlimited

  @Column(name = "times_used")
  @Builder.Default
  private Integer timesUsed = 0;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  public boolean isUsable() {
    LocalDateTime now = LocalDateTime.now();
    return isActive
        && !now.isBefore(validFrom)
        && !now.isAfter(validUntil)
        && (usageLimit == null || timesUsed < usageLimit);
  }
}
