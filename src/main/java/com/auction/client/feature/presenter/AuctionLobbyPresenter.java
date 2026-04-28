package com.auction.client.feature.presenter;

import javafx.scene.layout.FlowPane;

/**
 * Presenter for Auction Lobby view
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
            // TODO: Add auction cards to pane
        }
    }

    public void clearAuctions() {
        if (auctionPane != null) {
            auctionPane.getChildren().clear();
        }
    }
}
