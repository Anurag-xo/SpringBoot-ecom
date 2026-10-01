package in.anurag.CreatorStore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring will automatically configure a default ThreadPoolTaskExecutor.
    // You can customize it here later if needed (e.g., core pool size, queue capacity).
}
