package com.auction.server.service;

import com.auction.config.ConfigManager;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for password reset requests.
 * Prevents abuse by limiting requests per user within a time window.
 */
public class RateLimiter {
    private static final ConcurrentHashMap<String, RequestLog> requestLogs = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final int windowMinutes;

    public RateLimiter() {
        ConfigManager config = ConfigManager.getInstance();
        this.maxRequests = config.getRateLimitRequests();
        this.windowMinutes = config.getRateLimitWindowMinutes();
    }

    public boolean isAllowed(String userId) {
        LocalDateTime now = LocalDateTime.now();
        RequestLog log = requestLogs.getOrDefault(userId, new RequestLog());

        // Clean old requests outside the time window
        log.cleanOldRequests(now, windowMinutes);

        // Check if user exceeded the limit
        if (log.getRequestCount() >= maxRequests) {
            System.out.println("⚠️ Rate limit exceeded for user: " + userId);
            return false;
        }

        // Add new request
        log.addRequest(now);
        requestLogs.put(userId, log);
        return true;
    }

    public int getRemainingRequests(String userId) {
        RequestLog log = requestLogs.getOrDefault(userId, new RequestLog());
        log.cleanOldRequests(LocalDateTime.now(), windowMinutes);
        return Math.max(0, maxRequests - log.getRequestCount());
    }

    // Inner class to track requests
    private static class RequestLog {
        private final java.util.List<LocalDateTime> requests = new java.util.ArrayList<>();

        void addRequest(LocalDateTime time) {
            requests.add(time);
        }

        int getRequestCount() {
            return requests.size();
        }

        void cleanOldRequests(LocalDateTime now, int windowMinutes) {
            LocalDateTime cutoff = now.minusMinutes(windowMinutes);
            requests.removeIf(time -> time.isBefore(cutoff));
        }
    }
}
