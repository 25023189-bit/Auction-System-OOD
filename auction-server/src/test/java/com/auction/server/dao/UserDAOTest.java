package com.auction.server.dao;

import com.auction.common.model.User;
import com.auction.server.utils.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserDAOTest {

    @Test
    @DisplayName("Test lấy thông tin User theo ID")
    void testGetUserById() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("customer_id")).thenReturn("U001");
            when(mockRs.getString("username")).thenReturn("hanto");
            when(mockRs.getString("role")).thenReturn("BIDDER");

            UserDAO dao = new UserDAO();
            User user = dao.getUserById("U001");

            assertNotNull(user);
            assertEquals("hanto", user.getUsername());
        }
    }

    @Test
    @DisplayName("Test đăng ký User mới thành công")
    void testRegisterUser_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

            // Ép getUserByUsername trả về rỗng (chưa tồn tại)
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            // Giả lập luồng insert thành công
            when(mockStmt.executeUpdate()).thenReturn(1);

            UserDAO dao = new UserDAO();
            User newUser = new User("U999", "new_user", "BIDDER", "UET", 1000.0);

            String result = dao.registerUser(newUser, "password123");
            assertEquals("SUCCESS", result);
        }
    }

    @Test
    @DisplayName("Test đăng nhập với BCrypt khớp mật khẩu")
    void testLogin_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            String hashedPass = BCrypt.hashpw("mat_khau_chuan", BCrypt.gensalt());

            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("customer_id")).thenReturn("U001");
            when(mockRs.getString("password_hash")).thenReturn(hashedPass); // Giả lập DB trả về hash

            UserDAO dao = new UserDAO();
            User user = dao.login("U001", "mat_khau_chuan");

            assertNotNull(user);
        }
    }
}