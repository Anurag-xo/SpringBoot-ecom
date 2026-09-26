package in.anurag.CreatorStore.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RateLimitConfig {

  // AUTH ENDPOINTS: 5 requests per minute per IP (prevents brute-force login)
  private final Bandwidth authLimit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));

  // PUBLIC ENDPOINTS: 60 requests per minute per IP (product browsing, etc.)
  private final Bandwidth publicLimit =
      Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));

  // ADMIN ENDPOINTS: 30 requests per minute per IP
  private final Bandwidth adminLimit =
      Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));

  // In-memory storage of buckets per IP per endpoint type
  private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> publicBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> adminBuckets = new ConcurrentHashMap<>();

  public Bucket resolveAuthBucket(String ip) {
    return authBuckets.computeIfAbsent(ip, k -> Bucket.builder().addLimit(authLimit).build());
  }

  public Bucket resolvePublicBucket(String ip) {
    return publicBuckets.computeIfAbsent(ip, k -> Bucket.builder().addLimit(publicLimit).build());
  }

  public Bucket resolveAdminBucket(String ip) {
    return adminBuckets.computeIfAbsent(ip, k -> Bucket.builder().addLimit(adminLimit).build());
  }
}
