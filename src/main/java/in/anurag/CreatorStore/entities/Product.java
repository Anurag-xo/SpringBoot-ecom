package in.anurag.CreatorStore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Product name is required")
  @Column(nullable = false)
  private String name;

  private String description;

  private String category;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.")
  @Column(nullable = false)
  private BigDecimal price;

  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock cannot be less than 0")
  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  // TODO: relations
  @JsonIgnore // i dont want to know the oder item, im intrested to know what all fields the product
  // has but not the orders in which the prodct is present. this makes sure when im
  // fetching my product im not also get all the orders that are associated with it.
  @OneToMany(mappedBy = "product")
  private List<OrderItem> orderItems;
}

// annotations add certain type of meta information so that this particular class is extending more
// functionality
//
