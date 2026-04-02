package client.controllers;

import client.services.AuctionService;
import common.models.Person.Seller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

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

    @FXML
    public void handleCreateAuction(ActionEvent event) {
        String itemName = txtItemName.getText();
        String priceStr = txtStartingPrice.getText();

        if (itemName.trim().isEmpty() || priceStr.trim().isEmpty()) {
            lblStatus.setText("❌ Vui lòng điền đầy đủ thông tin!");
            lblStatus.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
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

            if (auctionService != null) {
                auctionService.createAuction(itemName, startingPrice);

                // Đóng cửa sổ Pop-up
                javafx.stage.Stage stage = (javafx.stage.Stage) txtItemName.getScene().getWindow();
                stage.close();
            }

        } catch (NumberFormatException e) {
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