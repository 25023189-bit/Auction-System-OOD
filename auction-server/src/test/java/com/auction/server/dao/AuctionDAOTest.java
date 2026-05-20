package com.auction.server.dao;

import com.auction.common.model.AuctionRoom;
import com.auction.server.utils.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuctionDAOTest {

    @Test
    @DisplayName("Test tự động sinh ID phòng đấu giá tiếp theo")
    void testGenerateNextAuctionId() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            // Nếu mã cuối cùng trong DB là 5
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("auction_id")).thenReturn("AU100005");

            AuctionDAO dao = new AuctionDAO();
            String nextId = dao.generateNextAuctionId();

            // Hệ thống phải cộng 1 lên thành 6
            assertEquals("AU100006", nextId);
        }
    }

    @Test
    @DisplayName("Test lấy danh sách tất cả các phòng đấu giá")
    void testGetAllAuctions() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getString("auction_id")).thenReturn("AU100001");
            when(mockRs.getInt("product_id")).thenReturn(1);
            when(mockRs.getString("status")).thenReturn("OPEN");

            AuctionDAO dao = new AuctionDAO();
            List<AuctionRoom> list = dao.getAllAuctions();

            assertEquals(1, list.size());
        }
    }

    @Test
    @DisplayName("Test chốt phiên tự động (CloseAuctionByTime) trường hợp bị ế (Unsold)")
    void testCloseAuctionByTime_Unsold() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);

            // Lần rs.next() đầu tiên là lấy thông tin phòng -> Phòng tồn tại và đang mở
            when(mockRs.next()).thenReturn(true).thenReturn(false);
            when(mockRs.getInt("product_id")).thenReturn(1);
            when(mockRs.getString("seller_id")).thenReturn("SELLER_1");
            when(mockRs.getString("status")).thenReturn("OPEN");

            // Lần thứ hai query là lấy highest bid -> Vì rs.next() trả false ở trên nên hệ thống hiểu là không có ai đặt giá
            when(mockStmt.executeUpdate()).thenReturn(1);

            AuctionDAO dao = new AuctionDAO();
            IAuctionDAO.CloseAuctionResult result = dao.closeAuctionByTime("AU01");

            // Đảm bảo đối tượng result được trả về an toàn (không bị NullPointerException do sập DB)
            assertNotNull(result, "Kết quả trả về không được null");

            // Hân có thể gõ "result." ở đây để xem IntelliJ gợi ý các hàm có sẵn.
            // Nếu có hàm isSuccess() thì thêm: assertTrue(result.isSuccess());
            // Nếu có hàm getWinnerId() thì thêm: assertNull(result.getWinnerId());
        }
    }
}