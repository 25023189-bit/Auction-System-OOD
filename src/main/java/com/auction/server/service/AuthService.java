package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

/**
 * Lớp AuthService đóng vai trò là Tầng Nghiệp Vụ (Business Logic Layer).
 * Đã được đồng bộ 100% với UserDAO và ClientHandler.
 */
public class AuthService {

    private UserDAO userDAO = new UserDAO();

    // SỬ DỤNG 3 THAM SỐ ĐỂ KHỚP VỚI CLIENT HANDLER
    public Message login(String customerId, String password, String role) {
        User user = userDAO.login(customerId, password, role);

        if (user != null) {
            // Nhét toàn bộ object user vào data để ClientHandler lấy được ID
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        } else {
            return new Message("LOGIN_FAIL", "SERVER", "Sai mã khách hàng, mật khẩu hoặc vai trò!");
        }
    }

    // KHỚP ĐỐI TƯỢNG USER TỪ CLIENT HANDLER
    public Message registerUser(User user, String rawPassword) {
        // Truyền null cho email/fullname vì form chưa yêu cầu
        String newId = userDAO.registerUser(user, null, null, rawPassword);

        if (newId != null) {
            return new Message("REGISTER_SUCCESS", "SERVER", newId);
        } else {
            return new Message("REGISTER_FAIL", "SERVER", "Lỗi tạo tài khoản! Vui lòng xem Console.");
        }
    }

    // ĐỔI TÊN HÀM VÀ CHUẨN HÓA THAM SỐ
    public Message resetPassword(String customerId, String data) {
        String[] parts = data.split(":");
        if (parts.length < 2) {
            return new Message("RESET_FAIL", "SERVER", "Dữ liệu không hợp lệ!");
        }

        String token = parts[0];
        String newPassword = parts[1];

        if (newPassword.length() < 6 || newPassword.contains(" ")) {
            return new Message("RESET_FAIL", "SERVER", "Mật khẩu quá yếu!");
        }

        boolean isUpdated = userDAO.resetPassword(customerId, token, newPassword);

        if (isUpdated) {
            return new Message("RESET_SUCCESS", "SERVER", "Đổi mật khẩu thành công!");
        } else {
            return new Message("RESET_FAIL", "SERVER", "Lỗi: Không thể đổi mật khẩu lúc này!");
        }
    }
}