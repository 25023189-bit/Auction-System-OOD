package com.auction.server.main;

import com.auction.server.utils.DatabaseConnection;
import com.auction.server.utils.PasswordUtil;
// SỬA: Import đúng địa chỉ model AuctionRoom
import com.auction.common.model.AuctionRoom;

import java.sql.Connection;

/**
 * Lớp Main dùng để chạy thử nghiệm (Test) các thành phần độc lập của Server
 * như Kết nối Database và mã hóa mật khẩu trước khi chạy Server chính thức.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== TEST BĂM MẬT KHẨU ===");
        String matKhauGoc = "admin123";
        // Giả sử PasswordUtil của bạn đã có hàm hashPassword
        String matKhauDaBam = PasswordUtil.hashPassword(matKhauGoc);

        System.out.println("Mật khẩu người dùng nhập: " + matKhauGoc);
        System.out.println("Mật khẩu đã băm (để lưu vào DB): " + matKhauDaBam);

        System.out.println("\n=== TEST KẾT NỐI DATABASE ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Đã kết nối Java với MySQL thành công rực rỡ!");
            }
        } catch (Exception e) {
            System.out.println("❌ Kết nối thất bại! Hãy kiểm tra lại XAMPP/MySQL hoặc tài khoản/mật khẩu DB.");
            System.out.println("Lỗi chi tiết: " + e.getMessage());
        }

        System.out.println("\n=== TEST MODEL AUCTION ===");
        // Khởi tạo thử một phòng đấu giá
        AuctionRoom testRoom = new AuctionRoom("AUC001", "Laptop Dell", 500.0);
        boolean bidResult = testRoom.placeNewBid("BD500001", 600.0); // Thử đặt giá 600$

        if (bidResult) {
            System.out.println("✅ Đặt giá thử nghiệm thành công! Giá mới: " + testRoom.getCurrentPrice());
        } else {
            System.out.println("❌ Đặt giá thử nghiệm thất bại (Giá quá thấp).");
        }
    }
}