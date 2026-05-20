package com.auction.server.service;

import com.auction.server.config.ConfigManager;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bộ giới hạn số lần request reset mật khẩu theo cửa sổ thời gian.
 *
 * Vai trò:
 * - Theo dõi timestamp các request reset password theo từng userId/username.
 * - Cho biết request hiện tại có được phép tiếp tục và còn bao nhiêu lượt.
 *
 * Luồng chính:
 * 1. Luồng reset mật khẩu gọi isAllowed(username) trước khi xử lý request.
 * 2. RateLimiter dọn request cũ ngoài window, kiểm tra quota rồi ghi nhận request mới nếu hợp lệ.
 *
 * Business rules:
 * - Số request tối đa và window thời gian lấy từ ConfigManager.
 * - Vượt giới hạn thì không ghi thêm request mới và trả false.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe một phần: map là ConcurrentHashMap nhưng RequestLog bên trong dùng ArrayList không synchronized.
 * - Dependency: ConfigManager, LocalDateTime, ConcurrentHashMap.
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

    /**
     * Nhật ký timestamp request reset password của một user.
     *
     * Vai trò:
     * - Lưu các mốc thời gian request còn nằm trong window rate limit.
     * - Dọn request cũ và trả số lượng request hiện tại.
     *
     * Luồng chính:
     * 1. RateLimiter lấy hoặc tạo RequestLog theo userId.
     * 2. RequestLog cleanOldRequests(), addRequest() và getRequestCount() cho quyết định quota.
     *
     * Business rules:
     * - Request cũ hơn cutoff window phải bị loại trước khi đếm quota.
     * - Count hiện tại là cơ sở để so với maxRequests của cấu hình.
     *
     * Ghi chú kỹ thuật:
     * - Không thread-safe: requests là ArrayList mutable, caller chưa synchronized quanh từng log.
     * - Dependency: LocalDateTime, ArrayList.
     */
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
