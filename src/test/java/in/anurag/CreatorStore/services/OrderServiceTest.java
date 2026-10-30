package in.anurag.CreatorStore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import in.anurag.CreatorStore.dto.CouponValidationResponse;
import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.entities.UserAddress;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ProductVariantRepository;
import in.anurag.CreatorStore.utils.TestDataUtil;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private EmailService emailService;
    @Mock private CouponService couponService; // ✅ NEW
    @Mock private AddressService addressService; // ✅ NEW
    @Mock private ShippingService shippingService; // ✅ NEW
    
    @InjectMocks private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private UserAddress testAddress;

    @BeforeEach
    void setUp() {
        testUser = TestDataUtil.createTestUser("testuser");
        testProduct = TestDataUtil.createTestProduct(1L, "Test Product", BigDecimal.valueOf(50.00), 100);
        testAddress = UserAddress.builder().id(1L).fullName("Test User").build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(1L); // ✅ NEW
        orderRequest.setItems(Arrays.asList(itemRequest));

        when(addressService.getAddressById(1L, testUser)).thenReturn(testAddress);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(shippingService.calculateShippingCost(any(BigDecimal.class))).thenReturn(BigDecimal.TEN);
        when(couponService.validateCoupon(any(), any())).thenReturn(new CouponValidationResponse(false, BigDecimal.ZERO, "No coupon"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(orderRequest, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo("Test User"); // ✅ Derived from address
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(110.00)); // 100 subtotal + 10 shipping
        assertThat(result.getShippingCost()).isEqualByComparingTo(BigDecimal.TEN);

        verify(addressService, times(1)).getAddressById(1L, testUser);
        verify(shippingService, times(1)).calculateShippingCost(any(BigDecimal.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(emailService, times(1)).sendOrderConfirmationEmail(any(User.class), any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void createOrder_ProductNotFound() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(1L);
        orderRequest.setItems(Arrays.asList(itemRequest));

        when(addressService.getAddressById(1L, testUser)).thenReturn(testAddress);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(orderRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void createOrder_InsufficientStock() {
        Product lowStockProduct = TestDataUtil.createTestProduct(1L, "Low Stock", BigDecimal.valueOf(50.00), 5);
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(10);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(1L);
        orderRequest.setItems(Arrays.asList(itemRequest));

        when(addressService.getAddressById(1L, testUser)).thenReturn(testAddress);
        when(productRepository.findById(1L)).thenReturn(Optional.of(lowStockProduct));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    @DisplayName("Should get orders by user")
    void getOrdersByUser_Success() {
        Order order1 = TestDataUtil.createTestOrder(1L, testUser, OrderStatus.PENDING);
        Order order2 = TestDataUtil.createTestOrder(2L, testUser, OrderStatus.CONFIRMED);

        when(orderRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(Arrays.asList(order1, order2));

        var result = orderService.getOrdersByUser(testUser);

        assertThat(result).hasSize(2);
        verify(orderRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_Success() {
        Order order = TestDataUtil.createTestOrder(1L, testUser, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }
}
