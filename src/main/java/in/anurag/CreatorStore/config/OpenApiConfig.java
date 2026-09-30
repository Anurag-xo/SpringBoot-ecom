package in.anurag.CreatorStore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customizeOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("CreatorStore API")
                .summary("E-commerce Backend API for Creators")
                .description(
                    "This API allows users to browse products, manage carts, place orders, leave"
                        + " reviews, and allows administrators to manage inventory and order"
                        + " statuses.")
                .version("1.0.0")
                .contact(new Contact().name("Anurag").email("admin@creatorstore.com")))
        // Add global security requirement (adds the "Authorize" button in Swagger UI)
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .description(
                            "Enter your JWT token here. (No need to add 'Bearer ' prefix, just"
                                + " paste the raw token)")));
  }
}
