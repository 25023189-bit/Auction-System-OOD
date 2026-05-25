package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
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

class BidDAOTest {

    @Test
    @DisplayName("Test đặt giá thành công khi đủ số dư và giá hợp lệ")
    void testPlaceBid_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockStmt.executeUpdate()).thenReturn(1);

            // Giả lập DB trả về thông tin phòng đấu giá (giá hiện tại 100, số dư 500)
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("product_id")).thenReturn("P01");
            when(mockRs.getDouble("current_price")).thenReturn(100.0);
            when(mockRs.getDouble("balance")).thenReturn(500.0);
            when(mockRs.getDouble("min_bid_increment")).thenReturn(10.0);

            BidDAO dao = new BidDAO();
            // Đặt 200 > (100 + 10) và < 500 số dư -> Hợp lệ
            IBidDAO.BidResult result = dao.placeBid("AU01", "USER_1", 200.0);

            assertTrue(result.isSuccess());
        }
    }

    @Test
    @DisplayName("Test đặt giá thất bại do không đủ số dư trong ví")
    void testPlaceBid_LowBalance() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true);
            when(mockRs.getDouble("current_price")).thenReturn(100.0);
            when(mockRs.getDouble("balance")).thenReturn(50.0); // Số dư ảo có 50$
            when(mockRs.getDouble("min_bid_increment")).thenReturn(10.0);

            BidDAO dao = new BidDAO();
            // Đặt 200$ nhưng trong ví chỉ có 50$ -> Báo lỗi
            IBidDAO.BidResult result = dao.placeBid("AU01", "USER_1", 200.0);

            assertFalse(result.isSuccess());
            assertEquals("Current balance is not enough for this bid amount.", result.getMessage());
        }
    }

    @Test
    @DisplayName("Test lấy lịch sử đặt giá của một phòng")
    void testGetHistoryByRoom() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt("bid_id")).thenReturn(1);
            when(mockRs.getString("auction_id")).thenReturn("AU01");
            when(mockRs.getString("bidder_id")).thenReturn("U01");
            when(mockRs.getDouble("bid_amount")).thenReturn(150.0);
            when(mockRs.getInt("bid_rank")).thenReturn(1);
            when(mockRs.getInt("is_highest")).thenReturn(1);
            when(mockRs.getTimestamp("bid_time")).thenReturn(new Timestamp(System.currentTimeMillis()));

            BidDAO dao = new BidDAO();
            List<BidTransaction> list = dao.getHistoryByRoom("AU01");

            assertEquals(1, list.size());
        }
    }
}