package com.auction.client.feature.controllers;

import com.auction.client.service.AuctionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.auction.common.model.AuctionRoom;

/**
 * Controller cho popup xem chi tiết sản phẩm từ lobby.
 *
 * Vai trò:
 * - Nhận roomId cần xem chi tiết và yêu cầu server trả dữ liệu sản phẩm/phòng.
 * - Bind dữ liệu phản hồi vào các label trong popup.
 *
 * Luồng chính:
 * 1. setAuctionService() gắn callback PRODUCT_DETAILS_SUCCESS thông qua AuctionService.
 * 2. setRoomId() lưu roomId, hiển thị trạng thái loading và gọi requestProductDetails(roomId).
 *
 * Business rules:
 * - Nếu server không trả dữ liệu thì popup phải hiển thị thông báo không tìm thấy sản phẩm.
 * - Callback phải cập nhật UI qua Platform.runLater vì response đến từ luồng mạng.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: label JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: AuctionService, Platform, FXML Label, AuctionRoom.
 */
public class ProductViewController {

    @FXML
    private Label lblProductName;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblStartingPrice;

    private AuctionService auctionService;
    // roomId đang được popup yêu cầu chi tiết, hữu ích khi cần đối chiếu phản hồi server.
    private String currentRoomId;

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;

        // Callback được AuctionController kích hoạt khi server trả PRODUCT_DETAILS_SUCCESS.
        this.auctionService.setProductDetailsCallback(data -> {
            Platform.runLater(() -> {
                if (data != null) {
                    try {
                        AuctionRoom product = (AuctionRoom) data;

                        if (lblProductName != null) lblProductName.setText(product.getItemName());
                        if (lblDescription != null) lblDescription.setText(product.getItemDescription());
                        if (lblStartingPrice != null)
                            lblStartingPrice.setText(String.format("%,.0f $", product.getStartingPrice()));
                    } catch (Exception e) {
                        if (lblDescription != null) lblDescription.setText(data.toString());
                    }
                } else {

                    // Đổi text báo không tìm thấy
                    if (lblDescription != null) lblDescription.setText("Product information not found!");

                    // Không có dữ liệu nghĩa là server không tìm thấy phòng/sản phẩm tương ứng.
                    if(lblDescription != null) lblDescription.setText("Product information not found!");
                }
            });
        });
    }

    public void setRoomId(String roomId) {
        this.currentRoomId = roomId;
        // Hiển thị trạng thái tạm thời trong lúc chờ server trả dữ liệu.
        if (lblDescription != null) {
            lblDescription.setText("Loading data from server...");
        }

        if (this.auctionService != null) {
            this.auctionService.requestProductDetails(roomId);
        }
    }
}
