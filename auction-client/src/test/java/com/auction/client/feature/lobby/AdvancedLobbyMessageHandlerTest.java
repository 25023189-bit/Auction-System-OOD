package com.auction.client.feature.lobby;

import com.auction.client.feature.controllers.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.shared.mapper.DisplayMapper;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdvancedLobbyMessageHandlerTest {
    private DisplayMapper<String, List<AuctionRoom>> mockRawMapper;
    private DisplayMapper<List<AuctionRoom>, List<LobbyRoomDisplayModel>> mockDisplayMapper;
    private LobbyRoomListRenderer mockRenderer;
    private AdvancedLobbyMessageHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockRawMapper = mock(DisplayMapper.class);
        mockDisplayMapper = mock(DisplayMapper.class);
        mockRenderer = mock(LobbyRoomListRenderer.class);
        handler = new AdvancedLobbyMessageHandler(mockRawMapper, mockDisplayMapper, mockRenderer);
    }

    @Test
    @DisplayName("Test hỗ trợ đúng loại action từ Server")
    void testSupports() {
        assertTrue(handler.supports("ROOM_LIST"));
        assertTrue(handler.supports("UPDATE_PRICE"));
        assertFalse(handler.supports("LOGIN_SUCCESS"));
    }

    @Test
    @DisplayName("Test xử lý sự kiện UPDATE_PRICE: Cắt chuỗi hợp lệ")
    void testHandleUpdatePrice_Success() {
        Message msg = new Message("UPDATE_PRICE", "SERVER", "ROOM_001|5500.0");
        handler.handle(msg);
        verify(mockRenderer).updatePrice("ROOM_001", 5500.0);
    }

    @Test
    @DisplayName("Test xử lý sự kiện UPDATE_PRICE an toàn khi dữ liệu bị rác")
    void testHandleUpdatePrice_InvalidData() {
        assertDoesNotThrow(() -> handler.handle(new Message("UPDATE_PRICE", "SERVER", null)));
        assertDoesNotThrow(() -> handler.handle(new Message("UPDATE_PRICE", "SERVER", "ROOM_001"))); // Thiếu giá
        assertDoesNotThrow(() -> handler.handle(new Message("UPDATE_PRICE", "SERVER", "ROOM_001|ABC"))); // Giá không phải số
        // Renderer không được gọi nếu dữ liệu rác
        verifyNoInteractions(mockRenderer);
    }

    @Test
    @DisplayName("Test xử lý sự kiện ROOM_LIST khi dữ liệu trả về rỗng")
    void testHandleRoomList_NullData() {
        Message msg = new Message("ROOM_LIST", "SERVER", null);
        when(mockDisplayMapper.map(anyList())).thenReturn(new ArrayList<>());

        handler.handle(msg);
        verify(mockRenderer).render(anyList());
    }

    @Test
    @DisplayName("Test xử lý sự kiện ROOM_LIST khi server trả về chuỗi String")
    void testHandleRoomList_StringData() {
        Message msg = new Message("ROOM_LIST", "SERVER", "DATA_STRING");
        when(mockRawMapper.map("DATA_STRING")).thenReturn(new ArrayList<>());
        when(mockDisplayMapper.map(anyList())).thenReturn(new ArrayList<>());

        handler.handle(msg);
        verify(mockRawMapper).map("DATA_STRING");
        verify(mockRenderer).render(anyList());
    }
}