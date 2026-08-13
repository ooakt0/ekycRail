package com.ekycrail.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * SpringDoc OpenAPI Configuration for eKYC Rail.
 * Configures Swagger/OpenAPI documentation with security schemes and API metadata.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("eKYC Rail API")
                        .version("0.0.1-SNAPSHOT")
                        .description("Zero-PII reactive microservice for digital identity verification in Nepal. " +
                                "All operations are non-blocking and designed for high-throughput processing.")
                        .contact(new Contact()
                                .name("eKYC Rail Team")
                                .url("https://ekycx.com")
                                .email("support@ekycx.com"))
                        .license(new License()
                                .name("Proprietary License")
                                .url("https://ekycx.com/license")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.dev.ekycx.com").description("Development"),
                        new Server().url("https://api.prod.ekycx.com").description("Production")
                ));
    }
}

