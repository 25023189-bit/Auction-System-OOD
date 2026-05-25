package com.auction.client.core.navigation;

import com.auction.client.feature.controllers.auction.notify.EndAuctionNotificationViewModel;
import com.auction.common.model.AuctionRoom;

/**
 * Hợp đồng điều hướng giữa các màn hình chính của client.
 *
 * Vai trò:
 * - Chuẩn hóa API đổi scene cho login, lobby và auction room.
 * - Cung cấp điểm mở dashboard seller/admin mà không lộ chi tiết FXMLLoader.
 *
 * Luồng chính:
 * 1. Controller hoặc message handler gọi method điều hướng tương ứng.
 * 2. Implementation quyết định FXML, Stage và dữ liệu session cần thiết.
 *
 * Business rules:
 * - showAuctionRoom() phải nhận AuctionRoom để màn hình mới có dữ liệu ban đầu.
 * - Dashboard phụ được mở độc lập, không thay thế scene chính.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; implementation JavaFX không thread-safe.
 * - Dependency: AuctionRoom và implementation như FxSceneNavigator.
 */
public interface SceneNavigator {
    void showLogin();

    void showLobby();

    void showAuctionRoom(AuctionRoom room);

    void showEndAuctionNotification(EndAuctionNotificationViewModel notificationData);

    void openSellerDashboard();

    void openAdminDashboard();
}
