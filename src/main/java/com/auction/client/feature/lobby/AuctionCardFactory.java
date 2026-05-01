package com.auction.client.feature.lobby;

import com.auction.common.model.AuctionRoom;
import com.auction.server.service.AuctionService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Factory đơn giản tạo card lobby trực tiếp từ AuctionRoom.
 *
 * Vai trò:
 * - Dựng VBox card hiển thị tên sản phẩm, roomId, giá hiện tại và nút Join Room.
 * - Gắn sự kiện joinRoom() vào nút tham gia phòng.
 *
 * Luồng chính:
 * 1. LobbyPresenter gọi create(room) cho từng AuctionRoom.
 * 2. Factory tạo các control JavaFX, gắn action và trả VBox cho FlowPane.
 *
 * Business rules:
 * - Nút Join Room phải gửi roomId của đúng card.
 * - Factory này dùng model server trực tiếp, không format qua LobbyRoomDisplayModel.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: tạo node JavaFX trên JavaFX Application Thread.
 * - Dependency: CardFactory, AuctionRoom, AuctionService, VBox, Label, Button.
 */
public class AuctionCardFactory implements CardFactory<AuctionRoom, VBox> {
    private final AuctionService auctionService;

    public AuctionCardFactory(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public VBox create(AuctionRoom room) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefSize(180, 180);
        card.setAlignment(Pos.CENTER);

        Label lblName = new Label("Product: " + room.getItemName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblId = new Label("ID: " + room.getRoomId());

        Label lblPrice = new Label("Current Price: " + room.getCurrentPrice());

        Button btnJoin = new Button("Join Room");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnJoin.setOnAction(e -> auctionService.joinRoom(room.getRoomId()));

        card.getChildren().addAll(lblName, lblId, lblPrice, btnJoin);
        return card;
    }
}
