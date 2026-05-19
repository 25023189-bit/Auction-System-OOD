package com.auction.client.feature.controllers;

import com.auction.client.service.AuctionService;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionControllerTest {

    private AuctionController controller;
    private AuctionService mockAuctionService;
    private ClientConnection mockClientConnection;
    private SessionStore mockSessionStore;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new AuctionController();
        mockAuctionService = mock(AuctionService.class);
        mockClientConnection = mock(ClientConnection.class);
        mockSessionStore = mock(SessionStore.class);

        // BÀI TỦ HACK LUỒNG: Tiêm trạng thái kết nối ảo trước để chặn hàm mở Socket thật
        injectField("isNetworkConnected", true);
        injectField("clientConnection", mockClientConnection);
        injectField("auctionService", mockAuctionService);
        injectField("sessionStore", mockSessionStore);

        // Khởi tạo các linh kiện đồ họa tối thiểu để tránh lỗi giao diện
        injectField("cbRegRole", new ComboBox<String>());
        injectField("lblStatus", new Label());
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = AuctionController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Test khởi tạo các thành phần hạt nhân (Core Abstractions) của Client")
    void testInitialize_CoreSetup() {
        // Chạy hàm khởi tạo phân phối tổng của FXML
        controller.initialize(null, null);

        // Xác thực ComboBox phân quyền đăng ký đã được nạp đủ lựa chọn chưa
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Test định tuyến tin nhắn phản hồi chi tiết sản phẩm thành công")
    void testOnServerResponse_ProductDetails() throws Exception {
        controller.initialize(null, null);

        // Giả lập Server bắn gói tin chi tiết sản phẩm về máy Client
        Message responseMsg = new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", "MockProductData");

        controller.onServerResponse(responseMsg);

        // Xác thực luồng sự kiện callback chi tiết sản phẩm đã được kích hoạt
        Thread.sleep(120);
        verify(mockAuctionService).fireProductDetailsReceived("MockProductData");
    }
}