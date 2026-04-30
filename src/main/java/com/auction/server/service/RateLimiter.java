package com.auction.server.service;

import com.auction.config.ConfigManager;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn số lần yêu cầu reset mật khẩu trong một khoảng thời gian.
 */
public class RateLimiter {
    // Dùng static map để mọi instance cùng chia sẻ log request.
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

        // Xóa request cũ nằm ngoài cửa sổ thời gian.
        log.cleanOldRequests(now, windowMinutes);

        // Từ chối nếu user đã vượt giới hạn.
        if (log.getRequestCount() >= maxRequests) {
            System.out.println("⚠️ Rate limit exceeded for user: " + userId);
            return false;
        }

        // Ghi nhận request mới.
        log.addRequest(now);
        requestLogs.put(userId, log);
        return true;
    }

    public int getRemainingRequests(String userId) {
        RequestLog log = requestLogs.getOrDefault(userId, new RequestLog());
        log.cleanOldRequests(LocalDateTime.now(), windowMinutes);
        return Math.max(0, maxRequests - log.getRequestCount());
    }

    // Lưu timestamp các request của một user.
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
