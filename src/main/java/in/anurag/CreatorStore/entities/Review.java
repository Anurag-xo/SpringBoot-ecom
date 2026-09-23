package in.anurag.CreatorStore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer rating; // 1 to 5

  @Column(length = 1000)
  private String comment;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore // Prevents serializing the entire User object
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore // Prevents serializing the entire Product object
  private Product product;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  // Expose only the username to the frontend to keep JSON clean
  @JsonProperty("username")
  public String getUsername() {
    return user != null ? user.getUsername() : "Unknown";
  }
}
