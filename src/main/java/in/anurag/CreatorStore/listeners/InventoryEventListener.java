package in.anurag.CreatorStore.listeners;

import in.anurag.CreatorStore.events.LowStockEvent;
import in.anurag.CreatorStore.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

  private final EmailService emailService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleLowStockEvent(LowStockEvent event) {
    log.info(
        "🎧 Event Listener: Processing LowStockEvent for Product #{} (Stock: {})",
        event.getProduct().getId(),
        event.getCurrentStock());
    try {
      emailService.sendLowStockAlert(event.getProduct(), event.getCurrentStock());
      log.info("✅ Low stock alert sent for Product #{}", event.getProduct().getId());
    } catch (Exception e) {
      log.error(
          "❌ Failed to send low stock alert for Product #{}: {}",
          event.getProduct().getId(),
          e.getMessage());
    }
  }
}
