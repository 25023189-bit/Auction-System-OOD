package com.auction.server.dao;

import com.auction.common.model.Item;
import com.auction.server.utils.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ItemDAOTest {

    @Test
    @DisplayName("saveItem without sellerId is rejected")
    void testSaveItem_NoSellerId() {
        ItemDAO dao = new ItemDAO();
        assertFalse(dao.saveItem(new Item()));
    }

    @Test
    @DisplayName("saveItem inserts product and reads generated product id")
    void testSaveItem_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockKeys = mock(ResultSet.class);

            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1);
            when(mockStmt.getGeneratedKeys()).thenReturn(mockKeys);
            when(mockKeys.next()).thenReturn(true);
            when(mockKeys.getInt(1)).thenReturn(1);

            ItemDAO dao = new ItemDAO();
            Item item = new Item("IT01", "Macbook", "Moi 100%", 1000.0);

            assertTrue(dao.saveItem(item, "SELLER_UET_01"));
        }
    }

    @Test
    @DisplayName("updateCurrentPrice updates product current price")
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
