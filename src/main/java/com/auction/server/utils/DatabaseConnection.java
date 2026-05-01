package com.auction.server.utils;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory tạo JDBC connection cho database MySQL của hệ thống đấu giá.
 *
 * Vai trò:
 * - Đọc cấu hình kết nối database từ environment, system property hoặc resource properties.
 * - Load JDBC driver và tạo Connection mới cho các DAO.
 *
 * Luồng chính:
 * 1. DAO gọi getConnection(), lớp này load cấu hình và chọn giá trị ưu tiên đầu tiên không rỗng.
 * 2. Validate url/user/driver, load driver rồi gọi DriverManager.getConnection().
 *
 * Business rules:
 * - Environment variable/system property được ưu tiên hơn db.properties để thuận tiện deploy.
 * - Thiếu cấu hình bắt buộc phải ném SQLException có thông tin chẩn đoán rõ ràng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng method static và biến local.
 * - Dependency: DriverManager, Properties, ClassLoader resource db.properties/db.properties.example.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = loadDatabaseProperties();

        // Cho phép override cấu hình khi deploy mà không cần sửa file resource.
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
            throw new SQLException("JDBC driver not found: " + driver, e);
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException(buildHelpfulConnectionError(url, user, driver), e);
        }
    }

    // Đọc db.properties thật, nếu không có thì dùng db.properties.example để hỗ trợ môi trường mẫu.
    private static Properties loadDatabaseProperties() throws SQLException {
        Properties props = new Properties();
        URL resource = findDatabasePropertiesResource();
        if (resource == null) {
            throw new SQLException(
                    "Database configuration was not found on the classpath. " +
                            "Add db.properties or db.properties.example under src/main/resources."
            );
        }

        try (InputStream input = resource.openStream()) {
            props.load(input);
            return props;
        } catch (Exception e) {
            throw new SQLException("Failed to read database configuration file.", e);
        }
    }

    // Tìm file cấu hình database trên classpath.
    private static URL findDatabasePropertiesResource() {
        ClassLoader classLoader = DatabaseConnection.class.getClassLoader();
        URL resource = classLoader.getResource("db.properties");
        return resource != null ? resource : classLoader.getResource("db.properties.example");
    }

    private static void validateRequired(String value, String key) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Missing required configuration: " + key);
        }
    }

    // Lấy giá trị đầu tiên không null/không rỗng trong danh sách ưu tiên.
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

    // Tạo lỗi có ngữ cảnh để dễ chẩn đoán sai cấu hình DB.
    private static String buildHelpfulConnectionError(String url, String user, String driver) {
        return "Database connection failed. Check that MySQL is running, database 'auction_system' exists, " +
                "and the connection configuration is correct. " +
                "[url=" + safe(url) + ", user=" + safe(user) + ", driver=" + safe(driver) + "]";
    }

    private static String safe(String value) {
        return value == null ? "<null>" : value;
    }
}
