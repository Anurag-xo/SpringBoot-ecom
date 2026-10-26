package in.anurag.CreatorStore.listeners;

import in.anurag.CreatorStore.events.ProductUpdatedEvent;
import in.anurag.CreatorStore.services.SearchService; // Your Meilisearch service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

  private final SearchService searchService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
    log.info(
        "🎧 Event Listener: Processing ProductUpdatedEvent for Product #{}",
        event.getProduct().getId());
    try {
      searchService.indexProduct(event.getProduct());
      log.info("✅ Product #{} indexed in Meilisearch successfully", event.getProduct().getId());
    } catch (Exception e) {
      log.error(
          "❌ Failed to index Product #{} in Meilisearch: {}",
          event.getProduct().getId(),
          e.getMessage());
    }
  }
}
