package in.anurag.CreatorStore.tasks;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.OrderStatus;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.repositories.OrderRepository;
import in.anurag.CreatorStore.repositories.ProductRepository;
import in.anurag.CreatorStore.services.EmailService;
import in.anurag.CreatorStore.services.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final EmailService emailService;

  @Value("${admin.email:admin@creatorstore.com}")
  private String adminEmail;

  @Scheduled(cron = "0 0 2 * * ?")
  public void checkLowStockAndAlert() {
    log.info("🕒 Running scheduled task: Check Low Stock");
    List<Product> lowStockProducts = productRepository.findByStockQuantityLessThan(10);

    if (!lowStockProducts.isEmpty()) {
      log.info(
          "⚠️ Found {} products with low stock. Sending alert to admin.", lowStockProducts.size());
      emailService.sendLowStockAlertEmail(adminEmail, lowStockProducts);
    } else {
      log.info("✅ All products have sufficient stock.");
    }
  }

  @Scheduled(cron = "0 0 3 * * ?")
  @Transactional
  public void cancelExpiredPendingOrders() {
    log.info("🕒 Running scheduled task: Cancel Expired Pending Orders");
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
    List<Order> expiredOrders =
        orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, twentyFourHoursAgo);

    if (expiredOrders.isEmpty()) {
      log.info("✅ No expired pending orders found.");
      return;
    }

    log.info(
        "⚠️ Found {} expired pending orders. Cancelling and restoring stock...",
        expiredOrders.size());
    for (Order order : expiredOrders) {
      try {
        orderService.cancelExpiredPendingOrder(order);
        emailService.sendOrderCancellationEmail(order.getUser(), order);
        log.info("✅ Successfully cancelled expired order ID: {}", order.getId());
      } catch (Exception e) {
        log.error("❌ Failed to cancel order ID {}: {}", order.getId(), e.getMessage());
      }
    }
  }
}
