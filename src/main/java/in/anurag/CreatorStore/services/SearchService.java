package in.anurag.CreatorStore.services;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.TaskInfo;
import in.anurag.CreatorStore.entities.Product;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

  private final Client meilisearchClient;
  private static final String INDEX_NAME = "products";

  @Async
  public void indexProduct(Product product) {
    try {
      Index index = meilisearchClient.index(INDEX_NAME);
      Map<String, Object> document = productToMap(product);
      TaskInfo task = index.addDocuments("[" + mapToJson(document) + "]");
      log.info(
          "📄 Indexed product '{}' to Meilisearch. Task UID: {}",
          product.getName(),
          task.getTaskUid());
    } catch (Exception e) {
      log.error("❌ Failed to index product '{}': {}", product.getName(), e.getMessage());
    }
  }

  @Async
  public void deleteProductFromIndex(Long productId) {
    try {
      Index index = meilisearchClient.index(INDEX_NAME);
      TaskInfo task = index.deleteDocument(String.valueOf(productId));
      log.info(
          "🗑️ Deleted product ID {} from Meilisearch index. Task UID: {}",
          productId,
          task.getTaskUid());
    } catch (Exception e) {
      log.error("❌ Failed to delete product {} from index: {}", productId, e.getMessage());
    }
  }

  @Async
  public void reindexAllProducts(List<Product> products) {
    try {
      Index index = meilisearchClient.index(INDEX_NAME);
      index.deleteAllDocuments();

      StringBuilder jsonBuilder = new StringBuilder("[");
      for (int i = 0; i < products.size(); i++) {
        jsonBuilder.append(mapToJson(productToMap(products.get(i))));
        if (i < products.size() - 1) {
          jsonBuilder.append(",");
        }
      }
      jsonBuilder.append("]");

      TaskInfo task = index.addDocuments(jsonBuilder.toString());
      log.info(
          "🔄 Reindexed {} products to Meilisearch. Task UID: {}",
          products.size(),
          task.getTaskUid());
    } catch (Exception e) {
      log.error("❌ Failed to reindex all products: {}", e.getMessage());
    }
  }

  // ✅ FIX 1: Return type changed from SearchResult to Searchable
  public Searchable searchProducts(String query, int limit) {
    try {
      Index index = meilisearchClient.index(INDEX_NAME);
      SearchRequest searchRequest = SearchRequest.builder().q(query).limit(limit).build();
      return index.search(searchRequest);
    } catch (Exception e) {
      log.error("❌ Search failed for query '{}': {}", query, e.getMessage());
      return null;
    }
  }

  // ✅ FIX 1 & 2: Return type changed, and filter now accepts String[]
  public Searchable searchProductsWithFilters(
      String query, String category, Double minPrice, Double maxPrice, int limit) {
    try {
      Index index = meilisearchClient.index(INDEX_NAME);

      List<String> filters = new ArrayList<>();
      if (category != null && !category.isBlank()) {
        filters.add("category = \"" + category + "\"");
      }
      if (minPrice != null) {
        filters.add("price >= " + minPrice);
      }
      if (maxPrice != null) {
        filters.add("price <= " + maxPrice);
      }

      SearchRequest.SearchRequestBuilder builder =
          SearchRequest.builder().q(query != null ? query : "").limit(limit);

      if (!filters.isEmpty()) {
        // ✅ FIX 2: Wrap the joined string in a String array
        builder.filter(new String[] {String.join(" AND ", filters)});
      }

      return index.search(builder.build());
    } catch (Exception e) {
      log.error("❌ Filtered search failed: {}", e.getMessage());
      return null;
    }
  }

  private Map<String, Object> productToMap(Product product) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", product.getId());
    map.put("name", product.getName());
    map.put("description", product.getDescription() != null ? product.getDescription() : "");
    map.put("category", product.getCategory() != null ? product.getCategory() : "");
    map.put("price", product.getPrice().doubleValue());
    map.put("stockQuantity", product.getStockQuantity());
    map.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");
    return map;
  }

  private String mapToJson(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder("{");
    int i = 0;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      sb.append("\"").append(entry.getKey()).append("\":");
      Object value = entry.getValue();
      if (value instanceof String) {
        sb.append("\"").append(escapeJson((String) value)).append("\"");
      } else {
        sb.append(value);
      }
      if (i < map.size() - 1) sb.append(",");
      i++;
    }
    sb.append("}");
    return sb.toString();
  }

  private String escapeJson(String text) {
    return text.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
