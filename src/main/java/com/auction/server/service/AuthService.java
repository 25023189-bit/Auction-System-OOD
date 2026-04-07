package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.utils.PasswordUtil;

/**
 * Lớp AuthService đóng vai trò là Tầng Nghiệp Vụ (Business Logic Layer).
 * Xử lý mọi thao tác liên quan đến định danh người dùng: Đăng nhập, Đăng ký, Đổi mật khẩu.
*/

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    public Message login(String username, String password) {
        // Kiểm tra trong SQL thay vì MockDB
        User user = userDAO.login(username, password);

        if (user != null) {
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        } else {
            return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    public Message register(String username, String password, String role) {
        System.out.println("Người dùng đã đăng ký:"+username);
        String newId = "BD5" + (System.currentTimeMillis() % 100000);
        String hashedPass = com.auction.server.utils.PasswordUtil.hashPassword(password);

        // 🌟 BƯỚC LÀM SẠCH VÀ ÉP KIỂU TUYỆT ĐỐI (BULLETPROOF)
        // Mặc định cứ đăng ký là cho thành BIDDER hết
        String finalRole = "BIDDER";

        // Nếu chuỗi Client gửi lên có chứa chữ "SELLER" (bất kể viết hoa/thường hay có dấu cách)
        if (role != null && role.toUpperCase().contains("SELLER")) {
            finalRole = "SELLER";
        }

        User newUser;
        if (finalRole.equals("SELLER")) {
            newUser = new com.auction.common.model.Seller(newId, username, hashedPass, 0.0);
        } else {
            newUser = new com.auction.common.model.Bidder(newId, username, hashedPass, 0.0);
        }

        // Ép chặt Role chuẩn vào đối tượng trước khi lưu
        newUser.setRole(finalRole);

        // Gọi xuống DAO
        if (userDAO.registerUser(newUser)) {
            return new Message("REGISTER_SUCCESS", "SERVER", newId);
        } else {
            return new Message("REGISTER_FAIL", "SERVER", "Lỗi tạo tài khoản! Vui lòng xem Console.");
        }
    }

    public Message resetPassword(String username, String newPassword) {
        // 1. Kiểm tra mật khẩu mới có đủ mạnh không
        if (newPassword == null || newPassword.length() < 6 || newPassword.contains(" ")) {
            return new Message("RESET_FAIL", "SERVER", "Mật khẩu quá yếu (Cần >= 6 ký tự và không có khoảng trắng)!");
        }

        // 2. Băm mật khẩu mới bằng BCrypt trước khi lưu
        String hashedNewPassword = PasswordUtil.hashPassword(newPassword);

        // 3. Gọi UserDAO để cập nhật xuống Database
        boolean isUpdated = userDAO.updatePassword(username, hashedNewPassword);

        if (isUpdated) {
            System.out.println("🔄 " + username + " vừa đổi mật khẩu mới trong Database.");
            return new Message("RESET_SUCCESS", "SERVER", "Đổi mật khẩu thành công!");
        } else {
            return new Message("RESET_FAIL", "SERVER", "Lỗi: Không tìm thấy tên đăng nhập này trong hệ thống!");
        }
    }
}