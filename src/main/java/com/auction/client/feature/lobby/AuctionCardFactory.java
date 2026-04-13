package com.auction.client.feature.lobby;

import com.auction.common.model.AuctionRoom;
import com.auction.server.service.AuctionService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AuctionCardFactory implements CardFactory<AuctionRoom, VBox> {
    private final AuctionService auctionService;

    public AuctionCardFactory(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public VBox create(AuctionRoom room) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefSize(180, 150);
        card.setAlignment(Pos.CENTER);

        // Hiển thị tạm ID Sản phẩm vì Model không còn lưu trực tiếp Tên
        // Thay getProductId() bằng getItemName() để hiện tên sản phẩm cho đẹp
        Label lblName = new Label("Sản phẩm: " + room.getItemName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

// Thay getAuctionId() bằng getRoomId() cho đúng tên biến trong AuctionRoom
        Label lblId = new Label("Mã: " + room.getRoomId());

        Button btnJoin = new Button("Vào Phòng");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

// Thay getAuctionId() bằng getRoomId() để truyền đúng ID phòng khi nhấn nút
        btnJoin.setOnAction(e -> auctionService.joinRoom(room.getRoomId()));

        card.getChildren().addAll(lblName, lblId, btnJoin);
        return card;
    }
}