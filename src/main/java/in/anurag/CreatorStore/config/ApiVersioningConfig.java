package in.anurag.CreatorStore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "api.version")
@Getter
@Setter
public class ApiVersioningConfig {
    
    private String current = "v1";
    private List<String> deprecated = List.of();
    
    /**
     * Check if a specific API version is deprecated
     */
    public boolean isDeprecated(String version) {
        return deprecated.contains(version);
    }
    
    /**
     * Get the base path for the current API version
     */
    public String getCurrentBasePath() {
        return "/api/" + current;
    }
}
