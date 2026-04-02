package com.auction.server.service;

// SỬA: Import đúng địa chỉ package thực tế
import com.auction.server.dao.UserDAO;
import com.auction.common.dto.Message;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;
import com.auction.server.dao.MockDB;

/**
 * Lớp AuthService đóng vai trò là Tầng Nghiệp Vụ (Business Logic Layer).
 * Xử lý mọi thao tác liên quan đến định danh người dùng: Đăng nhập, Đăng ký, Đổi mật khẩu.
 * * ⚠️ LƯU Ý:
 * 1. BẢO MẬT: Mật khẩu hiện đang lưu dưới dạng văn bản gốc (Plain-text). Bắt buộc phải
 * tích hợp thư viện mã hóa băm (như BCrypt) vào hàm register() và login() trước khi Golive.
 * 2. HIỆU NĂNG: Các hàm tìm kiếm đang dùng vòng lặp for-each duyệt qua toàn bộ values()
 * của HashMap. Độ phức tạp là O(N). Nếu có 1 triệu User, Server sẽ bị nghẽn.
 * -> Giải pháp: Cần tạo thêm một HashMap phụ (Index) ánh xạ từ Username -> UserID.
 */

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    /**
     * ĐĂNG NHẬP: Gọi trực tiếp UserDAO để check Database
     */
    /*public Message login(String email, String password) {
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
    }*/

    public Message login(String usernameOrId, String password) {
        System.out.println("--- Đang xử lý đăng nhập cho: " + usernameOrId + " ---");

        // TẠM THỜI ĐÓNG CODE KẾT NỐI DATABASE THẬT:
        // User user = userDAO.login(email, password);

        // SỬ DỤNG MOCKDB ĐỂ TEST TRƯỚC:
        for (User user : MockDB.userTable.values()) {
            // Kiểm tra khớp Username (Adam) và Password
            if (user.getUsername().equalsIgnoreCase(usernameOrId) && user.getPassword().equals(password)) {
                String role = user.getRole();
                System.out.println("=> Thành công! Role: " + role);
                return new Message("LOGIN_SUCCESS", role, (Object) user);
            }
        }

        return new Message("LOGIN_FAIL", "SERVER", (Object) "Sai tài khoản hoặc mật khẩu!");
    }


    /**
     * ĐĂNG KÝ
     */
    // Sửa lại tham số nhận vào có thêm role
    public Message register(String username, String password, String role) {
        // Sau này bạn nên bổ sung userDAO.register(username, password, role) ở đây
        //return new Message("REGISTER_SUCCESS", "SERVER", (Object) "Đăng ký thành công!");
        String payload = password + "|" + role;

        Message msg = new Message("REGISTER", username, payload);
        for (User user : MockDB.userTable.values()) {
            if (user.getUsername().equals(username)) {
                return new Message("REGISTER_FAIL", "SERVER", "Tên này có người xài rồi!");
            }
        }

        MockDB.userCounter++;
        String newId = String.format("BD5%05d", MockDB.userCounter);

        User newUser;
        if (role.equalsIgnoreCase("SELLER")) {
            newUser = new Seller(newId, username, password, 1000);
        } else {
            newUser = new Bidder(newId, username, password, 1000);
        }

        if (!newUser.isPasswordStrong()) {
            return new Message("REGISTER_FAIL", "SERVER", "Mật khẩu quá yếu (Cần >= 6 ký tự và không có khoảng trắng)!");
        }

        MockDB.userTable.put(newId, newUser);
        return new Message("REGISTER_SUCCESS", "SERVER", newId);
    }

    public Message resetPassword(String username, String newPassword) {
        // Sau này gọi userDAO.updatePassword(email, newPassword)
        for (User user : MockDB.userTable.values()) {
            if (user.getUsername().equals(username)) {
                user.setPassword(newPassword);
                System.out.println("🔄 " + username + " vừa đổi mật khẩu mới.");
                return new Message("RESET_SUCCESS", "SERVER", "OK");
            }
        }
        return new Message("RESET_FAIL", "SERVER", "Tài khoản không tồn tại!");
    }
}