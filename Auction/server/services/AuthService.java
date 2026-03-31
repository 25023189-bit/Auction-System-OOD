package server.services;

import common.DTO.Message;
import common.models.Person.Bidder;
import common.models.Person.Seller;
import common.models.Person.User;
import server.database.UserDAO; // Dùng cái này thay cho MockDB

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    /**
     * ĐĂNG NHẬP: Gọi trực tiếp UserDAO để check MySQL
     */
    public Message login(String email, String password) {
        System.out.println("--- Đang xử lý đăng nhập cho: " + email + " ---");

        // Nhờ UserDAO chui xuống database tìm
        User user = userDAO.login(email, password);

        if (user != null) {
            // Lấy role (BIDDER/SELLER/ADMIN) từ đối tượng User trả về
            String role = user.getRole();
            System.out.println("=> Thành công! Role: " + role);
            return new Message("LOGIN_SUCCESS", role, user.getId());
        }

        return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản hoặc mật khẩu!");
    }

    /**
     * ĐĂNG KÝ: Chỗ này sau này bạn nên viết thêm hàm saveUser vào UserDAO
     */
    public Message register(String username, String password, String role) {
        // Tạm thời bạn có thể giữ logic cũ hoặc viết thêm userDAO.register()
        // Nhưng nhớ là check trùng email/username qua SQL chứ không dùng vòng lặp for nữa nhé!
        return new Message("REGISTER_SUCCESS", "SERVER", "Đăng ký thành công!");
    }

    /**
     * ĐỔI MẬT KHẨU
     */
    public Message resetPassword(String email, String newPassword) {
        // Sau này viết thêm hàm userDAO.updatePassword(email, newPassword) vào UserDAO nhé
        System.out.println("🔄 " + email + " yêu cầu đổi mật khẩu.");
        return new Message("RESET_SUCCESS", "SERVER", "OK");
    }
}