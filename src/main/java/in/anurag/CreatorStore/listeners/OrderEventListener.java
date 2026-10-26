package in.anurag.CreatorStore.listeners;

import in.anurag.CreatorStore.events.OrderCreatedEvent;
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
public class OrderEventListener {

  private final EmailService emailService;

  // ✅ AFTER_COMMIT ensures the email is ONLY sent if the Order was successfully saved to the DB
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    log.info(
        "🎧 Event Listener: Processing OrderCreatedEvent for Order #{}", event.getOrder().getId());
    try {
      emailService.sendOrderConfirmationEmail(event.getOrder());
      log.info(
          "✅ Order confirmation email sent successfully for Order #{}", event.getOrder().getId());
    } catch (Exception e) {
      log.error(
          "❌ Failed to send order confirmation email for Order #{}: {}",
          event.getOrder().getId(),
          e.getMessage());
      // In a real enterprise app, you would push this to a dead-letter queue or retry mechanism
      // here
    }
  }
}
