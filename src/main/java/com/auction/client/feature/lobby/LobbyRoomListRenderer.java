package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
            VBox card = cardFactory.createDefault(model);
            card.setUserData(model.getRoomId());
            paneSelectAuction.getChildren().add(card);
        }
    }

    public void updatePrice(String roomId, double newPrice) {
        if (paneSelectAuction == null || roomId == null || roomId.isBlank()) return;

        for (Node node : paneSelectAuction.getChildren()) {
            if (!(node instanceof VBox card)) continue;

            Object userData = card.getUserData();
            if (userData == null || !roomId.equals(userData.toString())) continue;

            for (Node child : card.getChildren()) {
                if (child instanceof Label label) {
                    String text = label.getText();
                    if (text != null && (text.startsWith("Current Price:") || text.startsWith("Price:"))) {
                        label.setText("Price: " + newPrice);
                        return;
                    }
                }
            }
        }
    }
}
