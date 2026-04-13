package com.auction.server.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = loadDatabaseProperties();

        String url = firstNonBlank(
                System.getenv("AUCTION_DB_URL"),
                System.getProperty("auction.db.url"),
                props.getProperty("db.url")
        );

        String user = firstNonBlank(
                System.getenv("AUCTION_DB_USER"),
                System.getProperty("auction.db.user"),
                props.getProperty("db.user")
        );

        String password = firstNonBlank(
                System.getenv("AUCTION_DB_PASSWORD"),
                System.getProperty("auction.db.password"),
                props.getProperty("db.password")
        );

        String driver = firstNonBlank(
                System.getenv("AUCTION_DB_DRIVER"),
                System.getProperty("auction.db.driver"),
                props.getProperty("db.driver"),
                DEFAULT_DRIVER
        );

        validateRequired(url, "db.url");
        validateRequired(user, "db.user");
        if (password == null) {
            password = "";
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy JDBC driver: " + driver, e);
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException(buildHelpfulConnectionError(url, user, driver), e);
        }
    }

    private static Properties loadDatabaseProperties() throws SQLException {
        Properties props = new Properties();

        InputStream input = null;

        ClassLoader cl = DatabaseConnection.class.getClassLoader();

        input = cl.getResourceAsStream("db.properties");
        if (input == null) {
            input = cl.getResourceAsStream("db.properties.example");
        }

        if (input == null) {
            throw new SQLException(
                    "Không tìm thấy cấu hình database trên classpath. " +
                            "Cần có ít nhất một trong hai file: db.properties hoặc db.properties.example trong src/main/resources."
            );
        }

        try (InputStream in = input) {
            props.load(in);
            return props;
        } catch (Exception e) {
            throw new SQLException("Đọc file cấu hình database thất bại.", e);
        }
    }

    private static void validateRequired(String value, String key) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Thiếu cấu hình bắt buộc: " + key);
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String buildHelpfulConnectionError(String url, String user, String driver) {
        return "Kết nối database thất bại. " +
                "Kiểm tra MySQL đã chạy chưa, database 'auction_system' đã được tạo chưa, " +
                "và thông tin cấu hình có đúng không. " +
                "[url=" + safe(url) + ", user=" + safe(user) + ", driver=" + safe(driver) + "]";
    }

    private static String safe(String value) {
        return value == null ? "<null>" : value;
    }
}