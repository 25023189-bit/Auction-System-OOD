package com.auction.server.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    private MockedStatic<DriverManager> mockedDriverManager;

    @BeforeEach
    void setUp() {
        // Bật tính năng Mock các hàm Static của DriverManager trước mỗi bài test
        mockedDriverManager = Mockito.mockStatic(DriverManager.class);
    }

    @AfterEach
    void tearDown() {
        // Đóng Mock sau khi test xong để không ảnh hưởng đến các class test khác
        if (mockedDriverManager != null) {
            mockedDriverManager.close();
        }
    }

    @Test
    void testGetConnection_Success() throws SQLException {
        // Cài đặt kịch bản: Giả lập một Connection thành công
        Connection mockConnection = Mockito.mock(Connection.class);

        // Khi hệ thống gọi DriverManager.getConnection với bất kỳ tham số nào -> Trả về mockConnection
        mockedDriverManager.when(() -> DriverManager.getConnection(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
        )).thenReturn(mockConnection);

        // Thực thi hàm cần test
        Connection actualConnection = DatabaseConnection.getConnection();

        // Kiểm tra kết quả
        assertNotNull(actualConnection, "Connection không được phép null");
        assertEquals(mockConnection, actualConnection, "Phải trả về đúng Connection đã được mock");
    }

    @Test
    void testGetConnection_ThrowsSQLException() {
        // 1. TẠO LỖI TRƯỚC (Để tránh Java gọi ngầm DriverManager lúc đang mock)
        String fakeErrorMessage = "Mocked DB Error: Access denied";
        SQLException fakeException = new SQLException(fakeErrorMessage);

        // 2. RỒI MỚI MOCK HÀNH VI
        mockedDriverManager.when(() -> DriverManager.getConnection(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
        )).thenThrow(fakeException); // Nhét cái biến đã tạo sẵn vào đây

        // Thực thi hàm và bắt lỗi
        SQLException thrownException = assertThrows(SQLException.class, () -> {
            DatabaseConnection.getConnection();
        }, "Hàm getConnection phải ném ra SQLException khi lỗi mạng");

        // Kiểm tra xem class của Hân có bọc lỗi lại và thêm thông tin hữu ích không
        String actualMessage = thrownException.getMessage();

        assertTrue(actualMessage.contains("Database connection failed"),
                "Tin nhắn lỗi phải chứa thông báo cơ bản");
        assertTrue(actualMessage.contains("defaultdb"),
                "Tin nhắn lỗi phải bóc tách được tên database từ URL Aiven");
        assertTrue(actualMessage.contains(fakeErrorMessage),
                "Tin nhắn lỗi phải bao gồm cả nguyên nhân gốc từ MySQL");
    }
}