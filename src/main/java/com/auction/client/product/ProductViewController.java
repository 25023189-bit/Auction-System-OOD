package com.auction.client.product;

import com.auction.common.model.AuctionRoom;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller popup chi tiết sản phẩm khi dữ liệu đã có sẵn ở client.
 *
 * Vai trò:
 * - Nhận AuctionRoom đã có và hiển thị thông tin sản phẩm.
 * - Cập nhật tên, mô tả và giá khởi điểm lên các label FXML.
 *
 * Luồng chính:
 * 1. Caller tạo/nạp popup và gọi setProductData(room).
 * 2. Controller đọc dữ liệu AuctionRoom và gán vào label tương ứng.
 *
 * Business rules:
 * - Caller phải truyền AuctionRoom hợp lệ trước khi hiển thị dữ liệu.
 * - Lớp này không tự gọi server để lấy chi tiết sản phẩm.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: label JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: AuctionRoom, FXML Label.
 */
public class ProductViewController {
    @FXML private Label lblProductName;
    @FXML private Label lblDescription;
    @FXML private Label lblStartingPrice;

    public void setProductData(AuctionRoom room) {
        // Gán dữ liệu AuctionRoom trực tiếp lên các label của popup.
        lblProductName.setText(room.getItemName());
        lblDescription.setText(room.getItemDescription());
        lblStartingPrice.setText(String.valueOf(room.getStartingPrice()));
    }
}
