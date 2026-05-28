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
 * Builds auction room cards for lobby screens.
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
        VBox card = new VBox(9);
        card.setPrefSize(200, 286);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("auction-room-card");

        VBox imageFrame = new VBox();
        imageFrame.setAlignment(Pos.CENTER);
        imageFrame.getStyleClass().add("auction-room-image-frame");

        ImageView imgProduct = new ImageView();
        imgProduct.getStyleClass().add("auction-room-image");
        imgProduct.setFitWidth(160);
        imgProduct.setFitHeight(82);
        imgProduct.setPickOnBounds(true);
        imgProduct.setPreserveRatio(true);
        ImageUtils.applyBase64OrPlaceholder(imgProduct, model.getBase64Image());
        imageFrame.getChildren().add(imgProduct);

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

        Button btnJoin = new Button("Join Room");
        btnJoin.getStyleClass().add("auction-room-join-button");
        btnJoin.setOnAction(e -> auctionService.joinRoom(model.getRoomId()));

        Button btnDetails = new Button("View Details");
        btnDetails.getStyleClass().add("auction-room-details-button");
        btnDetails.setOnAction(event -> {
            try {
                new ProductPopupLauncher(this.auctionService).open(model.getRoomId());
            } catch (Exception e) {
                LOGGER.error("Failed to open product detail window.", e);
            }
        });

        card.getChildren().addAll(imageFrame, lblName, lblId, lblPrice, btnJoin, btnDetails);
        return card;
    }
}
