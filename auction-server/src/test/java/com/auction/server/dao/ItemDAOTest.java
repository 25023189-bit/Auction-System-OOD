package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ItemDAOTest {

    @Test
    @DisplayName("Test lưu Item báo lỗi nếu gọi hàm không có sellerId")
    void testSaveItem_NoSellerId() {
        ItemDAO dao = new ItemDAO();
        assertFalse(dao.saveItem(new Item()));
    }

    @Test
    @DisplayName("Test lưu Item thành công bằng cách giả lập Connection")
    void testSaveItem_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1); // Giả lập insert 1 dòng thành công

            ItemDAO dao = new ItemDAO();
            Item item = new Item("IT01", "Macbook", "Mới 100%", 1000.0);

            assertTrue(dao.saveItem(item, "SELLER_UET_01"));
        }
    }

    @Test
    @DisplayName("Test cập nhật giá hiện tại thành công")
    void testUpdateCurrentPrice_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1);

            ItemDAO dao = new ItemDAO();
            assertTrue(dao.updateCurrentPrice("IT01", 1500.0));
        }
    }
}