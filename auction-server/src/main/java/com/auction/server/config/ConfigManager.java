package com.auction.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration manager for system settings.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        loadConfiguration();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        // Try to load from properties file first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("✅ Configuration loaded from application.properties");
            }
        } catch (IOException e) {
            System.out.println("⚠️ Could not load application.properties, using environment variables");
        }

        // Override with environment variables (higher priority)
        String rateLimitRequests = System.getenv("AUCTION_RATE_LIMIT_REQUESTS");
        String rateLimitWindowMinutes = System.getenv("AUCTION_RATE_LIMIT_WINDOW_MINUTES");

        if (rateLimitRequests != null) properties.setProperty("rate.limit.requests", rateLimitRequests);
        if (rateLimitWindowMinutes != null) properties.setProperty("rate.limit.window.minutes", rateLimitWindowMinutes);
    }

    public int getRateLimitRequests() {
        return Integer.parseInt(properties.getProperty("rate.limit.requests", "5"));
    }

    public int getRateLimitWindowMinutes() {
        return Integer.parseInt(properties.getProperty("rate.limit.window.minutes", "5"));
    }
}
