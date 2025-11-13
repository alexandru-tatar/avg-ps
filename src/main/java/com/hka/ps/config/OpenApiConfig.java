package com.hka.ps.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI paymentServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Payment Service API")
            .version("v1")
            .description("REST API des Payment Service (PS) zur Autorisierung, Verbuchung und Rückerstattung von Zahlungen.")
            .contact(new Contact()
                .name("AVG Team 6")
                .email("team6@example.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8083")
                .description("Lokale Entwicklungsinstanz")));
  }
}
