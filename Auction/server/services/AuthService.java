package server.services;

import common.DTO.Message;
import common.models.Person.Bidder;
import common.models.Person.Seller;
import common.models.Person.User;
import server.database.MockDB;
import common.models.*;

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

    /**
     * Xử lý yêu cầu Đăng nhập từ Client.
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Message chứa kết quả (Thành công/Thất bại) và Role của user.
     */

    public Message login(String username, String password) {
        for (User user : MockDB.userTable.values()) {
            // Tìm thấy user và đúng mật khẩu
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {

                // Lấy ra tên Class (Sẽ trả về chuỗi "Bidder" hoặc "Seller")
                //String role = user.getClass().getSimpleName();

                // Trả Role về cho Client (Mình giấu Role vào trường id của Message nhé)
                //return new Message("LOGIN_SUCCESS", role, user);
                return new Message("LOGIN_SUCCESS", "SERVER", user);
            }
        }
        return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản hoặc mật khẩu!");
    }

    // Sửa lại tham số nhận vào có thêm role
    public Message register(String username, String password, String role) {
        String payload = password + "|" + role;

        Message msg = new Message("REGISTER", username, payload);
        for (User user : MockDB.userTable.values()) {
            if (user.getUsername().equals(username)) {
                return new Message("REGISTER_FAIL", "SERVER", "Tên này có người xài rồi!");
            }
        }

        MockDB.userCounter++;
        String newId = String.format("BD%06d", MockDB.userCounter);

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