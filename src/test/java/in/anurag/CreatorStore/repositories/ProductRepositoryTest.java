package in.anurag.CreatorStore.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.utils.TestDataUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest {

  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
  }

  @Test
  @DisplayName("Should find products by category")
  void findByCategoryIgnoreCase() {
    // Arrange
    Product product1 =
        TestDataUtil.createTestProduct(null, "Product 1", BigDecimal.valueOf(10.00), 50);
    product1.setCategory("Merch");
    Product product2 =
        TestDataUtil.createTestProduct(null, "Product 2", BigDecimal.valueOf(20.00), 30);
    product2.setCategory("Digital");
    productRepository.save(product1);
    productRepository.save(product2);

    // Act
    Page<Product> result =
        productRepository.findByCategoryIgnoreCase("merch", PageRequest.of(0, 10));

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCategory()).isEqualTo("Merch");
  }

  @Test
  @DisplayName("Should find products by name containing text")
  void findByNameContainingIgnoreCase() {
    // Arrange
    Product product1 =
        TestDataUtil.createTestProduct(null, "Creator Mug", BigDecimal.valueOf(10.00), 50);
    Product product2 =
        TestDataUtil.createTestProduct(null, "Creator T-Shirt", BigDecimal.valueOf(20.00), 30);
    Product product3 =
        TestDataUtil.createTestProduct(null, "Digital Course", BigDecimal.valueOf(99.00), 100);
    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);

    // Act
    Page<Product> result =
        productRepository.findByNameContainingIgnoreCase("creator", PageRequest.of(0, 10));

    // Assert
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("Should save and retrieve product")
  void saveAndFindById() {
    // Arrange
    Product product =
        TestDataUtil.createTestProduct(null, "Test Product", BigDecimal.valueOf(99.99), 50);

    // Act
    Product saved = productRepository.save(product);
    Product found = productRepository.findById(saved.getId()).orElse(null);

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("Test Product");
    assertThat(found.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
  }
}
