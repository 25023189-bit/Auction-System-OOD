package com.auction.client.feature.controllers.assistant.product;

import com.auction.client.feature.controllers.assistant.product.detail.ProductDetailBinder;
import com.auction.client.feature.controllers.assistant.product.detail.ProductDetailMapper;
import com.auction.client.feature.controllers.assistant.product.popup.ProductPopupLauncher;
import com.auction.client.service.AuctionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProductViewController {
    @FXML private Label lblProductName;
    @FXML private Label lblDescription;
    @FXML private Label lblStartingPrice;

    private AuctionService auctionService;
    private ProductDetailBinder detailBinder;
    private final ProductDetailMapper detailMapper = new ProductDetailMapper();
    private ProductDetailsResponseController responseController;

    @FXML
    public void initialize() {
        detailBinder = new ProductDetailBinder(lblProductName, lblDescription, lblStartingPrice);
    }

    public void setAuctionService(AuctionService auctionService) {
        ensureDetailBinderReady();
        this.auctionService = auctionService;
        responseController = new ProductDetailsResponseController(auctionService, detailMapper, detailBinder);
        responseController.listenAndBind();
    }

    public void setRoomId(String roomId) {
        ensureDetailBinderReady();
        detailBinder.showLoading();
        if (auctionService != null) {
            auctionService.requestProductDetails(roomId);
        }
    }

    public void openProductDetailPopUp(String roomId) throws Exception {
        new ProductPopupLauncher(auctionService).open(roomId);
    }

    public void closeWindow(ActionEvent actionEvent) {
        javafx.scene.Node source = (javafx.scene.Node) actionEvent.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void ensureDetailBinderReady() {
        if (detailBinder == null) {
            detailBinder = new ProductDetailBinder(lblProductName, lblDescription, lblStartingPrice);
        }
    }
}
