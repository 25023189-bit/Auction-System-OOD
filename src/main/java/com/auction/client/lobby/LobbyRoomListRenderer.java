package com.auction.client.lobby;

import com.auction.client.viewmodel.LobbyRoomDisplayModel;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class LobbyRoomListRenderer {
    private final FlowPane paneSelectAuction;
    private final AbstractAuctionCardFactory<LobbyRoomDisplayModel, VBox> cardFactory;

    public LobbyRoomListRenderer(FlowPane paneSelectAuction,
                                 AbstractAuctionCardFactory<LobbyRoomDisplayModel, VBox> cardFactory) {
        this.paneSelectAuction = paneSelectAuction;
        this.cardFactory = cardFactory;
    }

    public void render(List<LobbyRoomDisplayModel> models) {
        if (paneSelectAuction == null) return;

        paneSelectAuction.getChildren().clear();
        if (models == null) return;

        for (LobbyRoomDisplayModel model : models) {
            paneSelectAuction.getChildren().add(cardFactory.createDefault(model));
        }
    }
}