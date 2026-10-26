package in.anurag.CreatorStore.events;

import in.anurag.CreatorStore.entities.Product;
import org.springframework.context.ApplicationEvent;

public class ProductUpdatedEvent extends ApplicationEvent {
  private final Product product;

  public ProductUpdatedEvent(Object source, Product product) {
    super(source);
    this.product = product;
  }

  public Product getProduct() {
    return product;
  }
}
