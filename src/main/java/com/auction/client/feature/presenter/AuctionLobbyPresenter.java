package com.auction.client.feature.presenter;

import javafx.scene.layout.FlowPane;

/**
 * Presenter lobby phiên bản nhẹ, chỉ quản lý FlowPane chứa card đấu giá.
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
