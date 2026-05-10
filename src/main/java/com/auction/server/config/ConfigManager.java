package com.auction.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration manager for email and system settings.
 * Loads configuration from environment variables and config files.
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
        String emailFrom = System.getenv("AUCTION_EMAIL_FROM");
        String emailPassword = System.getenv("AUCTION_EMAIL_PASSWORD");
        String smtpHost = System.getenv("AUCTION_SMTP_HOST");
        String smtpPort = System.getenv("AUCTION_SMTP_PORT");
        String maxRetries = System.getenv("AUCTION_EMAIL_MAX_RETRIES");
        String tokenExpiration = System.getenv("AUCTION_TOKEN_EXPIRATION_MINUTES");

        if (emailFrom != null) properties.setProperty("email.from", emailFrom);
        if (emailPassword != null) properties.setProperty("email.password", emailPassword);
        if (smtpHost != null) properties.setProperty("smtp.host", smtpHost);
        if (smtpPort != null) properties.setProperty("smtp.port", smtpPort);
        if (maxRetries != null) properties.setProperty("email.max.retries", maxRetries);
        if (tokenExpiration != null) properties.setProperty("token.expiration.minutes", tokenExpiration);
    }

    public String getEmailFrom() {
        return properties.getProperty("email.from", "no-reply@auctionsystem.com");
    }

    public String getEmailPassword() {
        return properties.getProperty("email.password", "");
    }

    public String getSmtpHost() {
        return properties.getProperty("smtp.host", "smtp.gmail.com");
    }

    public int getSmtpPort() {
        return Integer.parseInt(properties.getProperty("smtp.port", "587"));
    }

    public int getEmailMaxRetries() {
        return Integer.parseInt(properties.getProperty("email.max.retries", "3"));
    }

    public int getTokenExpirationMinutes() {
        return Integer.parseInt(properties.getProperty("token.expiration.minutes", "60"));
    }

    public int getRateLimitRequests() {
        return Integer.parseInt(properties.getProperty("rate.limit.requests", "5"));
    }

    public int getRateLimitWindowMinutes() {
        return Integer.parseInt(properties.getProperty("rate.limit.window.minutes", "5"));
    }
}
