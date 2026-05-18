package com.auction.server.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @BeforeEach
    @AfterEach
    void clearSystemProperties() {
        // Dọn sạch cấu hình giả lập trước và sau mỗi bài test để không làm bẩn hệ thống
        System.clearProperty("auction.db.url");
        System.clearProperty("auction.db.user");
        System.clearProperty("auction.db.password");
        System.clearProperty("auction.db.driver");
    }

    @Test
    @DisplayName("Test Constructor Private bằng Reflection để lấy 100% Line Coverage")
    void testPrivateConstructor() throws Exception {
        Constructor<DatabaseConnection> constructor = DatabaseConnection.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DatabaseConnection instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Test cấu hình sai thông tin kết nối phải ném ra lỗi lỗi kết nối Database")
    void testGetConnection_Failure() {
        // Thay vì truyền chuỗi trống (bị hàm firstNonBlank bỏ qua và dùng file gốc),
        // mình truyền hẳn một URL bậy bạ để hệ thống nhận diện nhưng chắc chắn tạch khi kết nối!
        System.setProperty("auction.db.url", "jdbc:mysql://127.0.0.1:9999/non_existent_db_han");
        System.setProperty("auction.db.user", "wrong_user");

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);

        // Kiểm tra xem lỗi trả về có chứa câu cảnh báo chuẩn của hàm buildHelpfulConnectionError không
        assertTrue(exception.getMessage().contains("Database connection failed"), "Phải chứa câu báo lỗi kết nối thất bại");
        assertTrue(exception.getMessage().contains("database 'non_existent_db_han' exists"), "Phải bóc tách được tên DB lỗi ra");
    }

    @Test
    @DisplayName("Test cấu hình sai tên Driver dẫn đến lỗi ClassNotFoundException")
    void testGetConnection_DriverNotFound() {
        System.setProperty("auction.db.url", "jdbc:mysql://localhost:3306/auction_db");
        System.setProperty("auction.db.user", "root");
        System.setProperty("auction.db.driver", "com.auction.FakeDriver"); // Driver ma

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);
        assertTrue(exception.getMessage().contains("JDBC driver not found"));
    }

    @Test
    @DisplayName("Test bóc tách tên DB từ chuỗi URL chứa tham số hỏi chấm (?)")
    void testExtractDatabaseName_WithQueryParams() {
        // Cấu hình URL có dấu ? để ép luồng chạy qua nhánh queryStart >= 0 trong file gốc
        System.setProperty("auction.db.url", "jdbc:mysql://127.0.0.1:9999/test_query_db?useSSL=false&serverTimezone=UTC");
        System.setProperty("auction.db.user", "fake_user");

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);
        // Kiểm tra xem hàm buildHelpfulConnectionError đã bóc tách đúng chữ 'test_query_db' chưa
        assertTrue(exception.getMessage().contains("database 'test_query_db' exists"));
    }

    @Test
    @DisplayName("Test bóc tách tên DB từ chuỗi URL cấu hình kiểu dấu chấm phẩy (;)")
    void testExtractDatabaseName_WithAttributes() {
        // Cấu hình URL có dấu ; để ép luồng chạy qua nhánh attributesStart >= 0
        System.setProperty("auction.db.url", "jdbc:mysql://127.0.0.1:9999/test_semi_db;DatabaseName=test_semi_db");
        System.setProperty("auction.db.user", "fake_user");

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);
        assertTrue(exception.getMessage().contains("database 'test_semi_db' exists"));
    }

    @Test
    @DisplayName("Test bóc tách tên DB từ URL dị dạng không có dấu gạch chéo kép (//)")
    void testExtractDatabaseName_NoSlashes() {
        // Ép hostStart < 0 bằng cách xóa bỏ kí tự //
        System.setProperty("auction.db.url", "jdbc:mysql:localhost:9999/no_slashes_db");
        System.setProperty("auction.db.user", "fake_user");

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);
        assertTrue(exception.getMessage().contains("database 'no_slashes_db' exists"));
    }

    @Test
    @DisplayName("Test URL rỗng hoàn toàn hoặc kết thúc bằng dấu gạch chéo không có tên DB")
    void testExtractDatabaseName_EmptyOrNullName() {
        // Trường hợp URL kết thúc ngay sau dấu gạch chéo (databaseStart + 1 >= url.length())
        System.setProperty("auction.db.url", "jdbc:mysql://localhost:9999/");
        System.setProperty("auction.db.user", "fake_user");

        SQLException exception = assertThrows(SQLException.class, DatabaseConnection::getConnection);
        // Khi không tìm thấy tên DB, thông báo lỗi phải chứa câu lệnh chung chung 'the configured database exists'
        assertTrue(exception.getMessage().contains("the configured database exists"));
    }
}