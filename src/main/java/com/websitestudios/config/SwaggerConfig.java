package com.websitestudios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Swagger / OpenAPI 3.0 configuration.
 *
 * SECURITY:
 * - Only active in "dev" profile
 * - Completely disabled in "prod" profile
 * - Configured with JWT Bearer auth scheme (ready for Phase 6)
 *
 * Access:
 * Dev: http://localhost:8080/swagger-ui.html
 * Prod: BLOCKED (404)
 */
@Configuration
@Profile({ "dev", "default" }) // Only active in dev or default profile
public class SwaggerConfig {

    private static final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${ws.swagger.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI websiteStudiosOpenAPI() {

        log.info("Initializing Swagger/OpenAPI documentation");

        return new OpenAPI()
                // ──── API Info ────
                .info(new Info()
                        .title("Website Studios API")
                        .description(
                                "Backend API for Website Studios — a service platform for " +
                                        "Android apps, iOS apps, and websites. " +
                                        "Handles project request submissions and admin management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Website Studios")
                                .email("dev@websitestudios.com")
                                .url("https://websitestudios.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://websitestudios.com/terms")))

                // ──── Server ────
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Development Server")))

                // ──── Security Scheme (JWT Bearer) ────
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token")))

                // ──── Apply security globally (can be overridden per endpoint) ────
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}