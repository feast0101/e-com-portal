package com.example.producteventservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI/Swagger configuration for the product event service. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Product Event Service API")
                .version("0.0.1-SNAPSHOT")
                .description(
                    "Microservice for capturing and storing product transaction events from Kafka."
                        + " This service listens to the 'product-events' topic and stores all"
                        + " product CRUD operations in a database for audit, analytics, and"
                        + " reporting purposes.")
                .contact(
                    new Contact()
                        .name("Product Event Service Team")
                        .email("event-service@example.com")
                        .url("https://github.com/example/product-event-service"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
        .servers(
            List.of(new Server().url("http://localhost:8081").description("Development Server")));
  }
}
