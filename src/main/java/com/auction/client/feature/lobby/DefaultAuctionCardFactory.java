package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.server.service.AuctionService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuctionCardFactory implements AbstractAuctionCardFactory<LobbyRoomDisplayModel, VBox> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuctionCardFactory.class);

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
        card.setPrefSize(200, 210); // Đã tăng chiều cao lên 210 để nhét đủ 2 nút không bị lẹm
        card.setAlignment(Pos.CENTER);

        String style = highlighted
                ? "-fx-background-color: #fff8dc; -fx-padding: 20; -fx-border-color: #f39c12; -fx-border-radius: 5; -fx-background-radius: 5;"
                : "-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;";
        card.setStyle(style);

        Label lblName = new Label(model.getItemName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblId = new Label("ID: " + model.getRoomId());
        Label lblPrice = new Label("Price: " + model.getDisplayPrice());

        // NÚT 1: VÀO PHÒNG
        Button btnJoin = new Button("Join Room");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnJoin.setOnAction(e -> auctionService.joinRoom(model.getRoomId()));

        // NÚT 2: CHI TIẾT SẢN PHẨM
        Button btnDetails = new Button("View Details");
        btnDetails.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");

        btnDetails.setOnAction(event -> {
            try {
                // Tải file giao diện FXML
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/auctionprototype/product-view.fxml"));
                javafx.scene.Parent root = loader.load();

                // Lấy Controller TỪ THƯ MỤC CONTROLLERS và truyền mã phòng sang
                com.auction.client.feature.controllers.ProductViewController controller = loader.getController();
                controller.setRoomId(model.getRoomId());

                // Mở popup
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Chi tiết sản phẩm: " + model.getItemName());
                stage.setScene(new javafx.scene.Scene(root));

                // Khóa sảnh chính khi popup đang mở
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.show();
            } catch (Exception e) {
                LOGGER.error("Failed to open product detail window.", e);
            }
        });

        // Add cả 2 nút vào thẻ hiển thị
        card.getChildren().addAll(lblName, lblId, lblPrice, btnJoin, btnDetails);

        return card;
    }
}
