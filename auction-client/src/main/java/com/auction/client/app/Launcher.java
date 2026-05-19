package com.auction.client.app;

/**
 * Entry point phụ để khởi chạy client JavaFX.
 *
 * Vai trò:
 * - Cung cấp main class ổn định cho cấu hình build/run của client.
 * - Chuyển tiếp toàn bộ tham số khởi chạy sang Main hiện tại của ứng dụng.
 *
 * Luồng chính:
 * 1. JVM gọi Launcher.main(args) khi chạy client.
 * 2. Launcher gọi Main.main(args) để JavaFX tiếp tục khởi tạo giao diện.
 *
 * Business rules:
 * - Không tự tạo service, session hoặc scene tại lớp này.
 * - Luồng khởi chạy thực tế vẫn do com.auction.client.app.Main đảm nhiệm.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ gọi static main.
 * - Dependency: com.auction.client.app.Main.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
