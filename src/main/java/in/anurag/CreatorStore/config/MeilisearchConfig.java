package in.anurag.CreatorStore.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.TaskInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MeilisearchConfig {

  @Value("${meilisearch.url}")
  private String meilisearchUrl;

  @Value("${meilisearch.key}")
  private String meilisearchKey;

  @Bean
  public Client meilisearchClient() {
    return new Client(new Config(meilisearchUrl, meilisearchKey));
  }

  @PostConstruct
  public void configureIndex() {
    try {
      Client client = meilisearchClient();
      Index index = client.index("products");

      // Configure searchable, filterable, and sortable attributes
      Settings settings = new Settings();
      settings.setSearchableAttributes(new String[] {"name", "description", "category"});
      settings.setFilterableAttributes(new String[] {"category", "price", "stockQuantity"});
      settings.setSortableAttributes(new String[] {"price", "createdAt"});
      settings.setDisplayedAttributes(
          new String[] {
            "id", "name", "description", "category", "price", "stockQuantity", "imageUrl"
          });

      TaskInfo task = index.updateSettings(settings);
      log.info(
          "Meilisearch index 'products' configured successfully. Task UID: {}", task.getTaskUid());
    } catch (Exception e) {
      log.warn(
          "Could not configure Meilisearch index (is Meilisearch running?): {}", e.getMessage());
      log.warn("   Search functionality will be unavailable until Meilisearch is started.");
    }
  }
}
