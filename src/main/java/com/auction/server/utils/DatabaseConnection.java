package com.auction.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. Singleton Instance (Thêm volatile để đồng bộ hóa bộ nhớ giữa các Thread)
    private static volatile Connection connection = null;

    // 2. Thông số kết nối
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // Đổi lại mật khẩu máy bạn nếu cần

    // 3. Private constructor
    private DatabaseConnection() {}

    // 4. Thread-safe Singleton
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ [Database] Kết nối Database thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ [Database] Lỗi: Không tìm thấy Driver MySQL!");
            throw new SQLException(e);
        }
        return connection;
    }

    // Thêm hàm đóng kết nối an toàn khi server tắt
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 [Database] Đã đóng kết nối an toàn.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}