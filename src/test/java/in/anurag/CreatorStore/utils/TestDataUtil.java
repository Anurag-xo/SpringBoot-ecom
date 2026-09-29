package in.anurag.CreatorStore.utils;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class TestDataUtil {

  public static User createTestUser(String username) {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_USER");

    return User.builder()
        .id(1L)
        .username(username)
        .email(username + "@test.com")
        .password("encodedPassword")
        .roles(roles)
        .enabled(true)
        .build();
  }

  public static User createTestAdmin() {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_USER");
    roles.add("ROLE_ADMIN");

    return User.builder()
        .id(2L)
        .username("admin")
        .email("admin@test.com")
        .password("encodedPassword")
        .roles(roles)
        .enabled(true)
        .build();
  }

  public static Product createTestProduct(Long id, String name, BigDecimal price, Integer stock) {
    return Product.builder()
        .id(id)
        .name(name)
        .description("Test description")
        .category("Test Category")
        .price(price)
        .stockQuantity(stock)
        .build();
  }

  public static Order createTestOrder(Long id, User user, OrderStatus status) {
    Order order = new Order();
    order.setId(id);
    order.setCustomerName(user.getUsername());
    order.setCustomerEmail(user.getEmail());
    order.setStatus(status);
    order.setTotalPrice(BigDecimal.valueOf(100.00));
    order.setUser(user);
    return order;
  }
}
