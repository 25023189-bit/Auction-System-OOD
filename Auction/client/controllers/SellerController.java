package client.controllers;

import client.services.AuctionService;
import client.services.ClientConnection;
import common.DTO.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Lớp SellerController quản lý màn hình Tạo phiên đấu giá mới.
 * Đây là một cửa sổ phụ (Pop-up) nảy lên từ giao diện Màn hình chính.
 * Nhiệm vụ:
 * 1. Thu thập thông tin vật phẩm và giá từ Seller.
 * 2. Xác thực (Validate) dữ liệu cục bộ (Không để trống, giá phải là số > 0).
 * 3. Gọi AuctionService để gửi yêu cầu lên Server và đóng cửa sổ.
 */

public class SellerController {

    // Các trường nhập liệu được liên kết với file seller-view.fxml
    @FXML private TextField txtItemName;
    @FXML private TextField txtStartingPrice;
    @FXML private Label lblStatus;

    // Service dùng để giao tiếp với Server (Được truyền vào từ AuctionController)
    private AuctionService auctionService;

    /**
     * Hàm này được gọi bởi AuctionController để "Bơm" (Inject) AuctionService vào đây.
     * Nhờ vậy, SellerController dùng chung một kết nối mạng với màn hình chính.
     */
    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    /**
     * Sự kiện xảy ra khi Seller bấm nút "Tạo Phiên Đấu Giá".
     */
    @FXML
    private void handleCreateAuction() {
        // Lấy dữ liệu và xóa khoảng trắng 2 đầu
        String itemName = txtItemName.getText().trim();
        String priceStr = txtStartingPrice.getText().trim();

        // 1. KIỂM TRA ĐẦU VÀO (VALIDATION) - Check rỗng
        if (itemName.isEmpty() || priceStr.isEmpty()) {
            lblStatus.setText("❌ Vui lòng nhập đủ tên vật phẩm và giá!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            // 2. KIỂM TRA ĐẦU VÀO - Check định dạng số học
            double startingPrice = Double.parseDouble(priceStr);
            if (startingPrice <= 0) {
                lblStatus.setText("❌ Giá khởi điểm phải lớn hơn 0!");
                return;
            }

            // 3. GỬI LÊN SERVER
            if (auctionService != null) {
                // Gọi hàm bên Service để đóng gói và gửi Message "CREATE_AUCTION"
                auctionService.createAuction(itemName, startingPrice);

                /*
                 * TODO cho người phát triển sau (Cải tiến UX/UI):
                 * Hiện tại thiết kế đang là "Gửi lệnh tạo xong đóng luôn cửa sổ" (Optimistic UI).
                 * Nếu muốn chặt chẽ hơn, khoan hãy đóng cửa sổ này. Hãy đợi Server trả về
                 * "CREATE_AUCTION_SUCCESS" rồi mới đóng. Nếu Server trả về lỗi thì in ra lblStatus.
                 */

                // Đóng cửa sổ Pop-up của Seller
                javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
                stage.close();
            }

        } catch (NumberFormatException e) {
            // Nếu người dùng nhập chữ cái vào ô Giá tiền (VD: "mười ngàn")
            lblStatus.setText("❌ Giá khởi điểm phải là số!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}