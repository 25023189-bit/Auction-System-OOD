package com.auction.client.feature.lobby;

import com.auction.client.feature.controllers.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.service.AuctionService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory tạo card phòng đấu giá cho lobby bằng LobbyRoomDisplayModel.
 *
 * Vai trò:
 * - Dựng card có tên sản phẩm, roomId, giá, nút Join Room và nút View Details.
 * - Mở popup chi tiết sản phẩm qua FXML khi người dùng muốn xem thông tin phòng.
 *
 * Luồng chính:
 * 2. Factory dựng VBox card, gắn action joinRoom và mở ProductView popup.
 *
 * Business rules:
 * - Join Room phải gửi đúng roomId của model lên server.
 * - Popup chi tiết dùng stage modal để tránh thao tác lệch ngữ cảnh khi đang xem chi tiết.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: tạo Node/FXMLLoader/Stage JavaFX trên JavaFX Application Thread.
 * - Dependency: LobbyRoomDisplayModel, AuctionService, FXMLLoader, ProductViewController, SLF4J.
 */
public class DefaultAuctionCardFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuctionCardFactory.class);

    private final AuctionService auctionService;

    public DefaultAuctionCardFactory(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public VBox createDefault(LobbyRoomDisplayModel model) {
        return baseCard(model);
    }

    private VBox baseCard(LobbyRoomDisplayModel model) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 210); // Chiều cao đủ cho thông tin phòng và hai nút thao tác.
        card.setAlignment(Pos.CENTER);

        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label lblName = new Label(model.getItemName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblId = new Label("ID: " + model.getRoomId());
        Label lblPrice = new Label("Price: " + model.getDisplayPrice());

        // Nút vào phòng gửi request joinRoom lên server.
        Button btnJoin = new Button("Join Room");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnJoin.setOnAction(e -> auctionService.joinRoom(model.getRoomId()));

        // Nút chi tiết mở popup ProductView, dữ liệu chi tiết vẫn lấy từ server.
        Button btnDetails = new Button("View Details");
        btnDetails.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");

        btnDetails.setOnAction(event -> {
            try {
                // Tải file giao diện FXML cho popup chi tiết.
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/auctionprototype/fxml/product-view.fxml"));
                javafx.scene.Parent root = loader.load();

                // Truyền roomId để ProductViewController yêu cầu server trả dữ liệu sản phẩm.
                com.auction.client.feature.controllers.ProductViewController controller = loader.getController();
                controller.setAuctionService(this.auctionService);
                controller.setRoomId(model.getRoomId());

                // Mở popup chi tiết ở Stage riêng.
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Chi tiết sản phẩm: " + model.getItemName());
                stage.setScene(new javafx.scene.Scene(root));

                // Khóa cửa sổ chính khi popup đang mở để tránh thao tác lệch ngữ cảnh.
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.show();
            } catch (Exception e) {
                LOGGER.error("Failed to open product detail window.", e);
            }
        });

        // Gắn đầy đủ thông tin và các nút thao tác vào card.
        card.getChildren().addAll(lblName, lblId, lblPrice, btnJoin, btnDetails);

        return card;
    }
}
