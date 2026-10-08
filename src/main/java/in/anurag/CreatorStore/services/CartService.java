package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.AddToCartRequest;
import in.anurag.CreatorStore.dto.OrderItemRequest;
import in.anurag.CreatorStore.dto.OrderRequest;
import in.anurag.CreatorStore.dto.UpdateCartItemRequest;
import in.anurag.CreatorStore.entities.Cart;
import in.anurag.CreatorStore.entities.CartItem;
import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.ProductVariant;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.CartRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.repositories.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final OrderService orderService;

  public Cart getCart(User user) {
    return cartRepository.findByUser(user).orElseGet(() -> createCart(user));
  }

  private Cart createCart(User user) {
    Cart cart = Cart.builder().user(user).build();
    return cartRepository.save(cart);
  }

  @Transactional
  public Cart addToCart(AddToCartRequest request, User user) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    ProductVariant variant = null;
    BigDecimal price = product.getPrice();
    int availableStock = product.getStockQuantity();

    if (request.getVariantId() != null) {
      variant =
          variantRepository
              .findById(request.getVariantId())
              .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

      if (!variant.getProduct().getId().equals(product.getId())) {
        throw new RuntimeException("Variant does not belong to this product");
      }
      if (!variant.getActive()) {
        throw new RuntimeException("This variant is not available");
      }
      price = variant.getPrice();
      availableStock = variant.getStockQuantity();
    }

    if (availableStock < request.getQuantity()) {
      throw new RuntimeException("Not enough stock available");
    }

    Cart cart = getCart(user);

    CartItem existingItem =
        cart.getItems().stream()
            .filter(
                item ->
                    item.getProduct().getId().equals(product.getId())
                        && ((item.getVariant() == null && request.getVariantId() == null)
                            || (item.getVariant() != null
                                && item.getVariant().getId().equals(request.getVariantId()))))
            .findFirst()
            .orElse(null);

    if (existingItem != null) {
      existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
    } else {
      CartItem newItem =
          CartItem.builder()
              .cart(cart)
              .product(product)
              .variant(variant)
              .quantity(request.getQuantity())
              .price(price)
              .build();
      cart.getItems().add(newItem);
    }

    return cartRepository.save(cart);
  }

  @Transactional
  public Cart updateCartItem(Long itemId, UpdateCartItemRequest request, User user) {
    Cart cart = getCart(user);
    CartItem item =
        cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    item.setQuantity(request.getQuantity());
    return cartRepository.save(cart);
  }

  @Transactional
  public Cart removeCartItem(Long itemId, User user) {
    Cart cart = getCart(user);
    CartItem item =
        cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    cart.getItems().remove(item);
    return cartRepository.save(cart);
  }

  @Transactional
  public void clearCart(User user) {
    Cart cart = cartRepository.findByUser(user).orElse(null);
    if (cart != null) {
      cart.getItems().clear();
      cartRepository.save(cart);
    }
  }

  @Transactional
  public Order checkout(User user) {
    Cart cart = getCart(user);
    if (cart.getItems().isEmpty()) {
      throw new RuntimeException("Cannot checkout an empty cart");
    }

    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setCustomerName(user.getUsername());
    orderRequest.setCustomerEmail(user.getEmail());

    List<OrderItemRequest> itemRequests =
        cart.getItems().stream()
            .map(
                item -> {
                  OrderItemRequest req = new OrderItemRequest();
                  req.setProductId(item.getProduct().getId());
                  req.setQuantity(item.getQuantity());
                  if (item.getVariant() != null) {
                    req.setVariantId(item.getVariant().getId());
                  }
                  return req;
                })
            .collect(Collectors.toList());

    orderRequest.setItems(itemRequests);
    Order order = orderService.createOrder(orderRequest, user);
    clearCart(user);

    return order;
  }
}
