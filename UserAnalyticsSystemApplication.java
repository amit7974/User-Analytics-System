package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the User Analytics + Semantic Search Backend.
 * <p>
 * This application tracks user behavioral events, generates lightweight
 * vector embeddings for each event, and exposes analytics and
 * semantic-search style APIs on top of that data.
 */
@SpringBootApplication
public class UserAnalyticsSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAnalyticsSystemApplication.class, args);
    }
}
