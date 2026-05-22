package com.auction.server.utils;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory tạo JDBC connection cho database MySQL của hệ thống đấu giá.
 * ĐÃ SỬA ĐỔI: Gán cứng (Hardcode) trực tiếp thông tin lên Cloud Aiven để bỏ qua lỗi môi trường.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // =========================================================================
        // CÁCH 1: GÁN CỨNG THÔNG TIN CLOUD (HARDCODE)
        // Bỏ qua toàn bộ việc đọc file properties hay biến môi trường đang bị lỗi
        // =========================================================================

        String url = "jdbc:mysql://mysql-2d6c9860-vnu-66a9.j.aivencloud.com:13022/auction_system_v2?sslMode=REQUIRED";
        String user = "avnadmin";
        String password = "AVNS_cVgX4Q0XNC3iArZBnaY";
        String driver = DEFAULT_DRIVER;

        /* ---------- ĐOẠN CODE CŨ ĐÃ ĐƯỢC COMMENT LẠI ----------
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
        ------------------------------------------------------ */

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
            throw new SQLException(buildHelpfulConnectionError(url, user, driver, e), e);
        }
    }

    // Các hàm phụ trợ bên dưới vẫn giữ nguyên để không bị lỗi cú pháp,
    // mặc dù hiện tại hàm getConnection() tạm thời không gọi đến chúng nữa.

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

    private static String buildHelpfulConnectionError(String url, String user, String driver, SQLException cause) {
        String databaseName = extractDatabaseName(url);
        StringBuilder message = new StringBuilder("Database connection failed. Check that MySQL is running, ");
        if (databaseName == null) {
            message.append("the configured database exists, ");
        } else {
            message.append("database '").append(databaseName).append("' exists, ");
        }
        message.append("and the connection configuration is correct.");

        String causeMessage = cause.getMessage();
        if (causeMessage != null && !causeMessage.trim().isEmpty()) {
            message.append(" MySQL said: ").append(causeMessage.trim());
        }

        message.append(" [url=").append(safe(url))
                .append(", user=").append(safe(user))
                .append(", driver=").append(safe(driver))
                .append("]");
        return message.toString();
    }

    private static String extractDatabaseName(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        int hostStart = url.indexOf("//");
        int databaseStart = hostStart >= 0 ? url.indexOf('/', hostStart + 2) : url.indexOf('/');
        if (databaseStart < 0 || databaseStart + 1 >= url.length()) {
            return null;
        }

        int databaseEnd = url.length();
        int queryStart = url.indexOf('?', databaseStart + 1);
        if (queryStart >= 0) {
            databaseEnd = Math.min(databaseEnd, queryStart);
        }

        int attributesStart = url.indexOf(';', databaseStart + 1);
        if (attributesStart >= 0) {
            databaseEnd = Math.min(databaseEnd, attributesStart);
        }

        String databaseName = url.substring(databaseStart + 1, databaseEnd).trim();
        return databaseName.isEmpty() ? null : databaseName;
    }

    private static String safe(String value) {
        return value == null ? "<null>" : value;
    }
}