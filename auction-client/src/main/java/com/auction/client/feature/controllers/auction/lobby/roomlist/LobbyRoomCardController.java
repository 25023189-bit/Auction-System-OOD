package com.auction.client.feature.controllers.auction.lobby.roomlist;

import com.auction.client.feature.lobby.DefaultAuctionCardFactory;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import javafx.scene.layout.VBox;

public class LobbyRoomCardController {
    private final DefaultAuctionCardFactory cardFactory;

    public LobbyRoomCardController(DefaultAuctionCardFactory cardFactory) {
        this.cardFactory = cardFactory;
    }

    public VBox create(LobbyRoomDisplayModel model) {
        return cardFactory.createDefault(model);
    }
}
