package in.anurag.CreatorStore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.utils.TestDataUtil;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    testProduct = TestDataUtil.createTestProduct(1L, "Test Product", BigDecimal.valueOf(99.99), 50);
  }

  @Test
  @DisplayName("Should create product successfully")
  void createProduct_Success() {
    // Arrange
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    // Act
    Product result = productService.createProduct(testProduct);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Product");
    assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    verify(productRepository, times(1)).save(testProduct);
  }

  @Test
  @DisplayName("Should get product by ID successfully")
  void getProductById_Success() {
    // Arrange
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    // Act
    Product result = productService.getProductById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(productRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void getProductById_NotFound() {
    // Arrange
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> productService.getProductById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Product not found with id: 999");
  }

  @Test
  @DisplayName("Should get all products")
  void getAllProducts_Success() {
    // Arrange
    Product product2 =
        TestDataUtil.createTestProduct(2L, "Product 2", BigDecimal.valueOf(49.99), 30);
    when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

    // Act
    List<Product> result = productService.getAllProducts();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("Test Product");
    assertThat(result.get(1).getName()).isEqualTo("Product 2");
    verify(productRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should update product successfully")
  void updateProduct_Success() {
    // Arrange
    Product updatedDetails =
        TestDataUtil.createTestProduct(1L, "Updated Product", BigDecimal.valueOf(149.99), 100);
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(updatedDetails);

    // Act
    Product result = productService.updateProduct(1L, updatedDetails);

    // Assert
    assertThat(result.getName()).isEqualTo("Updated Product");
    assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.99));
    verify(productRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should delete product successfully")
  void deleteProduct_Success() {
    // Arrange
    doNothing().when(productRepository).deleteById(1L);

    // Act
    productService.deleteProduct(1L);

    // Assert
    verify(productRepository, times(1)).deleteById(1L);
  }
}
