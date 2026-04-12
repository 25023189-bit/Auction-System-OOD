package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    /**
     * Xử lý Đăng nhập
     * Đã lược bỏ tham số 'role' vì giao diện login của bạn không có ô chọn role.
     * Hệ thống sẽ tự tìm User trong DB và trả về đối tượng User kèm Role tương ứng.
     */
    public Message login(String customerId, String password) {
        System.out.println("\n[AuthService] Nhận yêu cầu đăng nhập:");
        System.out.println("  - Username: " + customerId);

        // Gọi DAO để kiểm tra trong Database
        User user = userDAO.login(customerId, password);

        if (user != null) {
            System.out.println("  - Kết quả: THÀNH CÔNG (Role: " + user.getRole() + ")");
            // Gửi toàn bộ đối tượng User về để Client lưu vào Session
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        } else {
            System.out.println("  - Kết quả: THẤT BẠI (Sai thông tin hoặc tài khoản không tồn tại)");
            return new Message("LOGIN_FAIL", "SERVER", "Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    /**
     * Xử lý Đăng ký tài khoản mới
     */
    public Message registerUser(User user, String rawPassword) {
        System.out.println("\n[AuthService] Nhận yêu cầu đăng ký:");
        // 🔥 Đã sửa getCustomerId() thành getUsername() và getId() cho chuẩn với model User của bạn
        System.out.println("  - ID mới: " + user.getId());
        System.out.println("  - Username mới: " + user.getUsername());
        System.out.println("  - Role mong muốn: " + user.getRole());

        // Gọi DAO thực hiện insert vào Database
        String newId = userDAO.registerUser(user, null, null, rawPassword);

        if (newId != null) {
            System.out.println("  - Kết quả: ĐĂNG KÝ THÀNH CÔNG");
            return new Message("REGISTER_SUCCESS", "SERVER", newId);
        } else {
            System.out.println("  - Kết quả: ĐĂNG KÝ THẤT BẠI (Có thể trùng tên tài khoản)");
            return new Message("REGISTER_FAIL", "SERVER", "Lỗi tạo tài khoản! Tên đăng nhập có thể đã tồn tại.");
        }
    }

    /**
     * Xử lý Khôi phục mật khẩu
     */
    public Message resetPassword(String customerId, String data) {
        System.out.println("\n[AuthService] Nhận yêu cầu đổi mật khẩu cho: " + customerId);

        // Dữ liệu data thường có định dạng "newPassword:confirmPassword"
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
        } else {
            System.out.println("  - Kết quả: ĐỔI MẬT KHẨU THẤT BẠI");
            return new Message("RESET_FAIL", "SERVER", "Không thể đổi mật khẩu. Vui lòng kiểm tra lại tên tài khoản!");
        }
    }
}