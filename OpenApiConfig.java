package com.example.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userAnalyticsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Analytics + Semantic Search API")
                        .description("Tracks user events, computes analytics, and supports embedding-based semantic search.")
                        .version("1.0.0")
                        .contact(new Contact().name("Analytics Platform Team")));
    }
}
