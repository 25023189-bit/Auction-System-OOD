package com.auction.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. Đảm bảo tên database là auction_system (khớp với Workbench của bạn)
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system";

    private static final String USER = "root";

    // 2. QUAN TRỌNG: Với XAMPP, hãy để TRỐNG mật khẩu như thế này
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        try {
            // Thêm dòng này để chắc chắn Driver đã được tải
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver MySQL! Hãy kiểm tra Maven/Library.");
            return null;
        }
    }
}