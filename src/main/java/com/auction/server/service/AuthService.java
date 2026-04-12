package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    // Khớp với logic ClientHandler gọi: id, pass, role
    public Message login(String customerId, String password, String role) {
        User user = userDAO.login(customerId, password, role);
        if (user != null) {
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        } else {
            return new Message("LOGIN_FAIL", "SERVER", "Sai mã, mật khẩu hoặc vai trò!");
        }
    }

    // Đổi tên hàm cho rõ nghĩa và khớp ClientHandler
    public Message registerUser(User user, String rawPassword) {
        String newId = userDAO.registerUser(user, null, null, rawPassword);
        if (newId != null) {
            return new Message("REGISTER_SUCCESS", "SERVER", newId);
        } else {
            return new Message("REGISTER_FAIL", "SERVER", "Lỗi tạo tài khoản!");
        }
    }

    public Message resetPassword(String customerId, String data) {
        String[] parts = data.split(":");
        if (parts.length < 2) return new Message("RESET_FAIL", "SERVER", "Dữ liệu lỗi!");
        boolean ok = userDAO.resetPassword(customerId, parts[0], parts[1]);
        return ok ? new Message("RESET_SUCCESS", "SERVER", "Thành công!")
                : new Message("RESET_FAIL", "SERVER", "Thất bại!");
    }
}