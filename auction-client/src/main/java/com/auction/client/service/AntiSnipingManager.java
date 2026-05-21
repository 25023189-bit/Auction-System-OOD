package com.auction.client.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Quản lý giải thuật Anti-sniping (Chống đặt giá đầu cơ giây cuối cùng).
 * Tự động kéo dài thời gian kết thúc của phiên đấu giá nếu phát hiện hành vi đặt giá sát giờ đóng phòng.
 */
public final class AntiSnipingManager {

    // Quy định khung thời gian nhạy cảm là 30 giây cuối cùng trước khi đóng phiên đấu giá
    private static final long SNIPING_WINDOW_MILLIS = 30 * 1000;

    // Quy định khoảng thời gian tự động gia hạn thêm cho phòng đấu giá là 2 phút
    private static final long EXTENSION_TIME_MILLIS = 2 * 60 * 1000;

    private AntiSnipingManager() {
        // Lớp tiện ích tĩnh không cho phép tạo thực thể bên ngoài cấu trúc hệ thống
    }

    /**
     * Tính toán và tự động gia hạn phiên đấu giá nếu phát hiện hành vi bắn tỉa sát nút.
     * @param currentEndTimeEpochMillis Thời điểm đóng phiên hiện tại (định dạng Epoch Milliseconds).
     * @return Thời điểm đóng phiên mới sau khi áp dụng thuật toán lọc thời gian.
     */
    public static long calculateAntiSnipingExtension(long currentEndTimeEpochMillis) {
        long currentSystemTimeMillis = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        long timeRemainingBeforeCloseMillis = currentEndTimeEpochMillis - currentSystemTimeMillis;

        // Nếu lượt đặt giá rơi trúng vào khoảng 30 giây cuối cùng trước khi phiên đóng cửa
        if (timeRemainingBeforeCloseMillis > 0 && timeRemainingBeforeCloseMillis <= SNIPING_WINDOW_MILLIS) {
            System.out.println("[HỆ THỐNG CHỐNG ĐẦU CƠ GIÂY CUỐI]: Phát hiện lượt đặt giá sát giờ! Tự động gia hạn phòng thêm 2 phút.");
            return currentEndTimeEpochMillis + EXTENSION_TIME_MILLIS;
        }

        // Trả về thời gian gốc ban đầu nếu hoạt động đặt giá diễn ra trong khung thời gian an toàn
        return currentEndTimeEpochMillis;
    }
}