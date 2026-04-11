package com.auction.client.feature.lobby;

import com.auction.client.core.ui.ViewPresenter;
import com.auction.common.model.AuctionRoom;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class LobbyPresenter implements ViewPresenter {
    private final FlowPane paneSelectAuction;
    private final CardFactory<AuctionRoom, VBox> cardFactory;

    public LobbyPresenter(FlowPane paneSelectAuction,
                          CardFactory<AuctionRoom, VBox> cardFactory) {
        this.paneSelectAuction = paneSelectAuction;
        this.cardFactory = cardFactory;
    }

    public void showRooms(List<AuctionRoom> rooms) {
        if (paneSelectAuction == null) return;

        paneSelectAuction.getChildren().clear();
        if (rooms == null) return;

        for (AuctionRoom room : rooms) {
            paneSelectAuction.getChildren().add(cardFactory.create(room));
        }
    }

    @Override
    public void clear() {
        if (paneSelectAuction != null) {
            paneSelectAuction.getChildren().clear();
        }
    }
}