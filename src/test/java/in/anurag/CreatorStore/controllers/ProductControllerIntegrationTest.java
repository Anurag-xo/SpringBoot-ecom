package in.anurag.CreatorStore.controllers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.utils.TestDataUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
  }

  @Test
  @DisplayName("GET /api/products - Should return paginated products")
  void getAllProducts_Success() throws Exception {
    // Arrange
    Product product1 =
        TestDataUtil.createTestProduct(null, "Product 1", BigDecimal.valueOf(10.00), 50);
    Product product2 =
        TestDataUtil.createTestProduct(null, "Product 2", BigDecimal.valueOf(20.00), 30);
    productRepository.save(product1);
    productRepository.save(product2);

    // Act & Assert
    mockMvc
        .perform(get("/api/products").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].name").value("Product 1"))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /api/products/{id} - Should return product by ID")
  void getProductById_Success() throws Exception {
    // Arrange
    Product product =
        TestDataUtil.createTestProduct(null, "Test Product", BigDecimal.valueOf(99.99), 50);
    Product savedProduct = productRepository.save(product);

    // Act & Assert
    mockMvc
        .perform(get("/api/products/" + savedProduct.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Product"))
        .andExpect(jsonPath("$.price").value(99.99));
  }

  @Test
  @DisplayName("GET /api/products/{id} - Should return 404 when product not found")
  void getProductById_NotFound() throws Exception {
    mockMvc.perform(get("/api/products/999")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("POST /api/products - Admin should create product")
  void createProduct_Admin_Success() throws Exception {
    // Arrange
    Product product =
        TestDataUtil.createTestProduct(null, "New Product", BigDecimal.valueOf(49.99), 100);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Product"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("POST /api/products - Regular user should NOT create product")
  void createProduct_User_Forbidden() throws Exception {
    // Arrange
    Product product =
        TestDataUtil.createTestProduct(null, "New Product", BigDecimal.valueOf(49.99), 100);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/products - Should filter by category")
  void getAllProducts_FilterByCategory() throws Exception {
    // Arrange
    Product product1 =
        TestDataUtil.createTestProduct(null, "Product 1", BigDecimal.valueOf(10.00), 50);
    product1.setCategory("Merch");
    Product product2 =
        TestDataUtil.createTestProduct(null, "Product 2", BigDecimal.valueOf(20.00), 30);
    product2.setCategory("Digital");
    productRepository.save(product1);
    productRepository.save(product2);

    // Act & Assert
    mockMvc
        .perform(get("/api/products").param("category", "Merch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].category").value("Merch"));
  }
}
