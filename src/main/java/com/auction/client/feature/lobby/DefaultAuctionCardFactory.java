package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.server.service.AuctionService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DefaultAuctionCardFactory implements AbstractAuctionCardFactory<LobbyRoomDisplayModel, VBox> {
    private final AuctionService auctionService;

    public DefaultAuctionCardFactory(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public VBox createDefault(LobbyRoomDisplayModel model) {
        return baseCard(model, false);
    }

    @Override
    public VBox createHighlighted(LobbyRoomDisplayModel model) {
        return baseCard(model, true);
    }

    private VBox baseCard(LobbyRoomDisplayModel model, boolean highlighted) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 170);
        card.setAlignment(Pos.CENTER);

        String style = highlighted
                ? "-fx-background-color: #fff8dc; -fx-padding: 20; -fx-border-color: #f39c12; -fx-border-radius: 5; -fx-background-radius: 5;"
                : "-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;";
        card.setStyle(style);

        Label lblName = new Label(model.getItemName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblId = new Label("ID: " + model.getRoomId());
        Label lblPrice = new Label("Price: " + model.getDisplayPrice());

        Button btnJoin = new Button("Join Room");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnJoin.setOnAction(e -> auctionService.joinRoom(model.getRoomId()));

        card.getChildren().addAll(lblName, lblId, lblPrice, btnJoin);
        return card;
    }
}
