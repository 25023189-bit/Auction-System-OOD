package server.database;

import common.models.Person.User; // Đảm bảo đường dẫn này khớp với file User của bạn
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Hàm kiểm tra đăng nhập bằng Email và Mật khẩu
     * Kết nối trực tiếp với bảng 'users' trong MySQL
     */
    public User login(String email, String password) {
        // Câu lệnh SQL: Tìm user khớp email và mật khẩu
        // Lưu ý: Trong thực tế bạn nên dùng Bcrypt để check password_hash
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password); // Nếu có hàm băm mật khẩu, hãy băm 'password' trước khi đưa vào đây

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Nếu tìm thấy, tạo đối tượng User và đổ dữ liệu từ DB vào
                    User user = new User();

                    // Lấy dữ liệu theo đúng tên cột trong file .sql của bạn
                    user.setId(rs.getString("customer_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role")); // Để biết là BIDDER hay SELLER
                    user.setStatus(rs.getString("status"));

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn UserDAO (login): " + e.getMessage());
        }
        return null; // Trả về null nếu không tìm thấy hoặc sai pass
    }

    /**
     * Hàm lấy thông tin chi tiết của một User dựa trên ID
     */
    public User getUserById(String customerId) {
        String sql = "SELECT * FROM users WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("customer_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}