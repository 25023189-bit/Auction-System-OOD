package com.auction.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Tên database của bạn là auction_db (dựa theo file SQL V5)
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system";

    private static final String USER = "root";

    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}