package com.auction.server.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    // 1. Singleton Instance (Thêm volatile để đồng bộ hóa bộ nhớ giữa các Thread)
    private static volatile Connection connection = null;

    // 2. Private constructor (Ngăn chặn khởi tạo từ bên ngoài)
    private DatabaseConnection() {}

    // 3. Thread-safe Singleton
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // TẢI FILE CẤU HÌNH (THAY THẾ CHO VIỆC HARDCODE)
                Properties props = new Properties();
                InputStream inputStream = DatabaseConnection.class
                        .getClassLoader()
                        .getResourceAsStream("db.properties");

                if (inputStream == null) {
                    System.err.println("❌ [Database] LỖI CRITICAL: Không tìm thấy file db.properties!");
                    System.err.println("👉 Hãy vào thư mục src/main/resources, copy db.properties.example thành db.properties và điền mật khẩu của bạn vào.");
                    throw new SQLException("Thiếu file cấu hình database (db.properties).");
                }

                // Load dữ liệu từ file vào biến props
                props.load(inputStream);

                // Lấy thông số kết nối từ file
                // Cố tình thêm tham số characterEncoding ở đây để hỗ trợ tiếng Việt có dấu luôn cho an toàn
                String url = props.getProperty("db.url") + "?useUnicode=true&characterEncoding=UTF-8";
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");

                // Load Driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Khởi tạo kết nối thực tế
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("✅ [Database] Kết nối Database thành công với user: " + user);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ [Database] Lỗi: Không tìm thấy Driver MySQL (Thiếu thư viện mysql-connector-j)!");
            throw new SQLException(e);
        } catch (Exception e) {
            System.err.println("❌ [Database] Lỗi kết nối hoặc đọc file cấu hình!");
            e.printStackTrace();
            throw new SQLException(e);
        }
        return connection;
    }

    // 4. Hàm đóng kết nối an toàn khi server tắt
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 [Database] Đã đóng kết nối an toàn.");
            } catch (SQLException e) {
                System.err.println("❌ [Database] Lỗi khi đóng kết nối!");
                e.printStackTrace();
            }
        }
    }
}