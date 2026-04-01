package com.auction.server.service;

// SỬA: Import đúng địa chỉ package thực tế
import com.auction.server.dao.UserDAO;
import com.auction.common.dto.Message;
import com.auction.common.model.User;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    /**
     * ĐĂNG NHẬP: Gọi trực tiếp UserDAO để check Database
     */
    public Message login(String email, String password) {
        System.out.println("--- Đang xử lý đăng nhập cho: " + email + " ---");

        // Nhờ UserDAO tìm trong database
        User user = userDAO.login(email, password);

        if (user != null) {
            // Lấy role (BIDDER/SELLER/ADMIN)
            String role = user.getRole();
            System.out.println("=> Thành công! Role: " + role);

            // Sử dụng Constructor: Message(String action, String id, Object data)
            // Truyền Role vào ID, và UserID vào Data
            return new Message("LOGIN_SUCCESS", role, (Object) user.getId());
        }

        return new Message("LOGIN_FAIL", "SERVER", (Object) "Sai tài khoản hoặc mật khẩu!");
    }

    /**
     * ĐĂNG KÝ
     */
    public Message register(String username, String password, String role) {
        // Sau này bạn nên bổ sung userDAO.register(username, password, role) ở đây
        return new Message("REGISTER_SUCCESS", "SERVER", (Object) "Đăng ký thành công!");
    }

    /**
     * ĐỔI MẬT KHẨU
     */
    public Message resetPassword(String email, String newPassword) {
        // Sau này gọi userDAO.updatePassword(email, newPassword)
        System.out.println("🔄 " + email + " yêu cầu đổi mật khẩu.");
        return new Message("RESET_SUCCESS", "SERVER", (Object) "OK");
    }
}