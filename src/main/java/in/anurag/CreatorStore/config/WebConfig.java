package in.anurag.CreatorStore.config;

import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Map the /uploads/** URL path to the physical "uploads" folder on your disk
    String uploadPath = Paths.get("uploads").toAbsolutePath().normalize().toString();

    registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath + "/");
  }
}
