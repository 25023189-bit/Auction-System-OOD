package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TransactionDAOTest {

    @Test
    @DisplayName("Test lấy lịch sử đấu giá giả lập từ DB")
    void testGetHistoryByRoom() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            // Giả lập ResultSet có 1 dòng dữ liệu trả về
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("auction_id")).thenReturn("AU01");
            when(mockRs.getString("bidder_id")).thenReturn("BIDDER_HAN");
            when(mockRs.getDouble("bid_amount")).thenReturn(2000.0);
            when(mockRs.getTimestamp("bid_time")).thenReturn(new Timestamp(System.currentTimeMillis()));

            TransactionDAO dao = new TransactionDAO();
            List<BidTransaction> history = dao.getHistoryByRoom("AU01");

            assertEquals(1, history.size());
            assertEquals("BIDDER_HAN", history.get(0).getBidderId());
        }
    }

    @Test
    @DisplayName("Test lấy danh sách toàn bộ sản phẩm")
    void testGetAllItems() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("product_id")).thenReturn("P01");
            when(mockRs.getString("product_name")).thenReturn("Đồng hồ");
            when(mockRs.getDouble("current_price")).thenReturn(500.0);

            TransactionDAO dao = new TransactionDAO();
            List<Item> items = dao.getAllItems();

            assertEquals(1, items.size());
            assertEquals("P01", items.get(0).getId());
        }
    }
}