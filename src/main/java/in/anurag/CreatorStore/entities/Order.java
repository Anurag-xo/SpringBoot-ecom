package in.anurag.CreatorStore.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(nullable = false)
  private String status;

  @Column(name = "toatl_price", nullable = false)
  private BigDecimal totalPrice;

  @OneToMany(mappedBy = "order")
  private List<OrderItem> OrderItems;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  public void prePresist() {
    this.createdAt = LocalDateTime.now();
  }
}
