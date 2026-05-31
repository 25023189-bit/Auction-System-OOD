package com.auction.client.feature.lobby;

import com.auction.client.feature.controllers.assistant.product.popup.ProductPopupLauncher;
import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.service.AuctionService;
import com.auction.client.shared.utils.ImageUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
        card.setPrefSize(200, 248);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("auction-room-card");

        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        ImageView imgProduct = new ImageView();
        imgProduct.getStyleClass().add("auction-room-image");
        imgProduct.setFitWidth(160);
        imgProduct.setFitHeight(82);
        imgProduct.setPickOnBounds(true);
        ImageUtils.applyBase64OrPlaceholder(imgProduct, model.getBase64Image());

        Label lblName = new Label(model.getItemName());
        lblName.getStyleClass().add("auction-room-title");
        lblName.setWrapText(true);
        lblName.setMaxWidth(160);
        lblName.setAlignment(Pos.CENTER);

        Label lblId = new Label("ID: " + model.getRoomId());
        lblId.getStyleClass().add("auction-room-meta");
        lblId.setWrapText(true);
        lblId.setMaxWidth(160);
        lblId.setAlignment(Pos.CENTER);

        Label lblPrice = new Label("Price: " + model.getDisplayPrice());
        lblPrice.getStyleClass().add("auction-room-price");
        lblPrice.setWrapText(true);
        lblPrice.setMaxWidth(160);
        lblPrice.setAlignment(Pos.CENTER);

        // Nút vào phòng gửi request joinRoom lên server.
        Button btnJoin = new Button("Join Room");
        btnJoin.getStyleClass().add("auction-room-join-button");
        btnJoin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnJoin.setOnAction(e -> auctionService.joinRoom(model.getRoomId()));

        // Nút chi tiết mở popup ProductView, dữ liệu chi tiết vẫn lấy từ server.
        Button btnDetails = new Button("View Details");
        btnDetails.getStyleClass().add("auction-room-details-button");
        btnDetails.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");

        btnDetails.setOnAction(event -> {
            try {
                new ProductPopupLauncher(this.auctionService).open(model.getRoomId());
            } catch (Exception e) {
                LOGGER.error("Failed to open product detail window.", e);
            }
        });
        // Gắn đầy đủ thông tin và các nút thao tác vào card.
        card.getChildren().addAll(imgProduct, lblName, lblId, lblPrice, btnJoin, btnDetails);

        return card;
    }
}

