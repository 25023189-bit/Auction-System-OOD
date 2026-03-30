import utils.DatabaseConnection;
import utils.PasswordUtil;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== TEST BĂM MẬT KHẨU ===");
        String matKhauGoc = "admin123";
        String matKhauDaBam = PasswordUtil.hashPassword(matKhauGoc);

        System.out.println("Mật khẩu người dùng nhập: " + matKhauGoc);
        System.out.println("Mật khẩu đã băm (để lưu vào DB): " + matKhauDaBam);

        System.out.println("\n=== TEST KẾT NỐI DATABASE ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Đã kết nối Java với MySQL thành công rực rỡ!");
        } catch (Exception e) {
            System.out.println("❌ Kết nối thất bại! Hãy kiểm tra lại mật khẩu MySQL hoặc xem MySQL đã bật chưa.");
            System.out.println("Lỗi chi tiết: " + e.getMessage());
        }
    }
}