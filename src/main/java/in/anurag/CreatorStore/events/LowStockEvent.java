package in.anurag.CreatorStore.events;

import in.anurag.CreatorStore.entities.Product;
import org.springframework.context.ApplicationEvent;

public class LowStockEvent extends ApplicationEvent {
  private final Product product;
  private final int currentStock;

  public LowStockEvent(Object source, Product product, int currentStock) {
    super(source);
    this.product = product;
    this.currentStock = currentStock;
  }

  public Product getProduct() {
    return product;
  }

  public int getCurrentStock() {
    return currentStock;
  }
}
