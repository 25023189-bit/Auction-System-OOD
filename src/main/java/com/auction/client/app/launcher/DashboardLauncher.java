package com.auction.client.app.launcher;

/**
 * Hợp đồng mở các dashboard phụ ngoài màn hình chính.
 *
 * Vai trò:
 * - Chuẩn hóa thao tác mở seller/admin dashboard từ controller chính.
 * - Tách cách load FXML/Stage cụ thể khỏi nơi gọi.
 *
 * Luồng chính:
 * 1. AuctionController giữ DashboardLauncher theo vai trò cần mở.
 * 2. Khi user bấm nút, controller gọi launch() trên implementation tương ứng.
 *
 * Business rules:
 * - Implementation phải tự gắn service/session cần thiết cho controller dashboard.
 * - Dashboard phụ không được làm mất scene chính của người dùng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: các implementation như SellerDashboardLauncher và AdminDashboardLauncher.
 */
public interface DashboardLauncher {
    void launch();
}
