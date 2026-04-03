package com.auction.client.controllers;

import com.auction.server.service.AuctionService;
import com.auction.common.model.Seller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

/**
 * Lớp SellerController quản lý màn hình Tạo phiên đấu giá mới.
 * Đây là một cửa sổ phụ (Pop-up) nảy lên từ giao diện Màn hình chính.
 * Nhiệm vụ:
 * 1. Thu thập thông tin vật phẩm và giá từ Seller.
 * 2. Xác thực (Validate) dữ liệu cục bộ (Không để trống, giá phải là số > 0).
 * 3. Gọi AuctionService để gửi yêu cầu lên Server và đóng cửa sổ.
 */

public class SellerController {

    @FXML private TextField txtItemName;
    @FXML private TextField txtStartingPrice;
    @FXML private Label lblStatus;

    // Khai báo nhãn hiển thị số dư và uy tín
    @FXML private Label lblBalance;
    @FXML private Label lblReputation;

    @FXML private TextField txtRoomIdToClose;

    private AuctionService auctionService;
    private Seller currentSeller;

    /**
     * Hàm này được gọi bởi AuctionController để "Bơm" (Inject) AuctionService vào đây.
     * Nhờ vậy, SellerController dùng chung một kết nối mạng với màn hình chính.
     */
    public void setAuctionService(AuctionService service) {
        this.auctionService = service;
    }

    // Bơm dữ liệu Seller vào để hiển thị lên giao diện
    public void setSellerData(Seller seller) {
        this.currentSeller = seller;
        if (seller != null) {
            lblBalance.setText("Số dư ví: " + String.format("%,.0f VNĐ", seller.getBalance()));
            lblReputation.setText("Uy tín: " + String.format("%.1f ⭐", seller.getRatingScore()));
        }
    }

    /**
     * Sự kiện xảy ra khi Seller bấm nút "Tạo Phiên Đấu Giá".
     */
    @FXML
    public void handleCreateAuction() {
        // 1. THÊM ĐOẠN NÀY ĐỂ CHẶN LỖI NULL POINTER
        if (this.auctionService == null) {
            lblStatus.setText("❌ Lỗi: Service bị null!");
            System.err.println("⚠️ LỖI: auctionService chưa được truyền vào SellerController. Có phải bạn đang chạy 'test nóng' trực tiếp file seller-view.fxml mà bỏ qua bước Đăng nhập không?");
            return;
        }

        String itemName = txtItemName.getText();
        String priceStr = txtStartingPrice.getText();

        if (itemName.trim().isEmpty() || priceStr.trim().isEmpty()) {
            lblStatus.setText("❌ Vui lòng điền đầy đủ thông tin!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }
        try {
            // 2. KIỂM TRA ĐẦU VÀO - Check định dạng số học
            double startingPrice = Double.parseDouble(priceStr);
            if (startingPrice <= 0) {
                lblStatus.setText("❌ Giá khởi điểm phải lớn hơn 0!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            // Kiểm tra uy tín trước khi cho tạo phòng
            if (currentSeller != null && !currentSeller.isTrustworthy()) {
                lblStatus.setText("❌ CẢNH BÁO: Uy tín quá thấp (< 2.0 sao). Bạn bị cấm tạo phiên đấu giá!");
                lblStatus.setTextFill(javafx.scene.paint.Color.RED);
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

    @FXML
    public void handleCloseAction(ActionEvent event) {
        String roomId = txtRoomIdToClose.getText().trim();

        if (roomId.isEmpty()) {
            lblStatus.setText("❌ Vui lòng nhập Mã phòng cần chốt đơn!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (auctionService != null) {
            // Gọi hàm chốt phòng đã có sẵn trong AuctionService
            auctionService.closeAuction(roomId);

            lblStatus.setText("✅ Đã gửi lệnh chốt đơn cho phòng: " + roomId);
            lblStatus.setTextFill(javafx.scene.paint.Color.GREEN);

            // Xóa rỗng ô nhập sau khi chốt
            txtRoomIdToClose.clear();
        } else {
            lblStatus.setText("❌ Lỗi: Chưa kết nối được tới Service!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}