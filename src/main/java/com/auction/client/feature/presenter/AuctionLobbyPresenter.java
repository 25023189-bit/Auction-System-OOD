package com.auction.client.feature.presenter;

import javafx.scene.layout.FlowPane;

/**
 * Presenter lobby legacy quản lý FlowPane chứa card đấu giá.
 *
 * Vai trò:
 * - Cung cấp API nhẹ để clear hoặc display danh sách auction trong FlowPane.
 * - Giữ tương thích cho luồng presenter cũ.
 *
 * Luồng chính:
 * 1. Controller legacy gắn FlowPane bằng setAuctionPane().
 * 2. displayAuctions() hoặc clearAuctions() xóa nội dung hiện tại của pane.
 *
 * Business rules:
 * - displayAuctions() hiện chỉ clear pane; render card cụ thể nằm ở renderer/factory mới hơn.
 * - Nếu auctionPane null thì bỏ qua an toàn.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: FlowPane JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: FlowPane.
 */
public class AuctionLobbyPresenter {
    private FlowPane auctionPane;

    public AuctionLobbyPresenter() {
    }

    public void setAuctionPane(FlowPane pane) {
        this.auctionPane = pane;
    }

    public void displayAuctions(java.util.List<?> auctions) {
        if (auctionPane != null) {
            auctionPane.getChildren().clear();
            // Danh sách card cụ thể được triển khai ở các renderer/factory mới hơn.
        }
    }

    public void clearAuctions() {
        if (auctionPane != null) {
            auctionPane.getChildren().clear();
        }
    }
}
