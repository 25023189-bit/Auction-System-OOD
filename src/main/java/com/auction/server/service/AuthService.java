package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public Message login(String username, String password) {
        System.out.println("\n[AuthService] Nhận yêu cầu đăng nhập:");
        System.out.println("  - Username: " + username);

        User user = userDAO.login(username, password);

        if (user != null) {
            System.out.println("  - Kết quả: THÀNH CÔNG (Role: " + user.getRole() + ")");
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        }

        System.out.println("  - Kết quả: THẤT BẠI");
        return new Message("LOGIN_FAIL", "SERVER", "Sai tên đăng nhập hoặc mật khẩu!");
    }

    public Message registerUser(User user, String rawPassword) {
        System.out.println("\n[AuthService] Nhận yêu cầu đăng ký:");
        System.out.println("  - ID mới: " + user.getId());
        System.out.println("  - Username mới: " + user.getUsername());

        // Nhận kết quả là String từ DAO
        String resultStatus = userDAO.registerUser(user, rawPassword);

        if ("SUCCESS".equals(resultStatus)) {
            System.out.println(" - Kết quả: ĐĂNG KÝ THÀNH CÔNG");
            return new Message("REGISTER_SUCCESS", "SERVER", user.getUsername());
        }
        else if ("DUPLICATE".equals(resultStatus)) {
            System.out.println(" - Kết quả: TRÙNG LẶP DỮ LIỆU");
            return new Message("REGISTER_FAIL", "SERVER", "Tên đăng nhập đã có người sử dụng!");
        }
        else {
            // NẾU LÀ LỖI DATABASE, NÓ SẼ IN THẲNG LÊN MÀN HÌNH ĐỎ CỦA CLIENT
            System.out.println(" - Kết quả: LỖI DATABASE - " + resultStatus);
            return new Message("REGISTER_FAIL", "SERVER", resultStatus);
        }
    }
    public Message resetPassword(String customerId, String data) {
        System.out.println("\n[AuthService] Nhận yêu cầu đổi mật khẩu cho: " + customerId);

        String[] parts = data.split(":");
        if (parts.length < 2) {
            return new Message("RESET_FAIL", "SERVER", "Dữ liệu yêu cầu không hợp lệ!");
        }

        String newPassword = parts[0];
        String confirmPassword = parts[1];

        boolean isSuccess = userDAO.resetPassword(customerId, newPassword, confirmPassword);

        if (isSuccess) {
            System.out.println("  - Kết quả: ĐỔI MẬT KHẨU THÀNH CÔNG");
            return new Message("RESET_SUCCESS", "SERVER", "Đổi mật khẩu thành công!");
        }

        System.out.println("  - Kết quả: ĐỔI MẬT KHẨU THẤT BẠI");
        return new Message("RESET_FAIL", "SERVER", "Không thể đổi mật khẩu. Vui lòng kiểm tra lại dữ liệu!");
    }
}