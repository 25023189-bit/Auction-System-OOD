package com.auction.client.product;

import com.auction.common.model.AuctionRoom;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller hiển thị chi tiết sản phẩm khi dữ liệu đã có sẵn ở client.
 */
public class ProductViewController {
    @FXML
    private Label lblProductName;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblStartingPrice;

    public void setProductData(AuctionRoom room) {
        // Gán dữ liệu AuctionRoom trực tiếp lên các label của popup.
        lblProductName.setText(room.getItemName());
        lblDescription.setText(room.getItemDescription());
        lblStartingPrice.setText(String.valueOf(room.getStartingPrice()));
    }
}
