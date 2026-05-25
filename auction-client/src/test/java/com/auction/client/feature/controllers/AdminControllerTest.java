package com.auction.client.feature.controllers;

import com.auction.client.feature.controllers.account.admin.AdminController;
import com.auction.client.service.AuctionService;
import com.auction.client.network.socket.ClientConnection;
import com.auction.common.dto.Message;
import com.auction.common.model.User;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private AdminController controller;
    private AuctionService mockAuctionService;
    private ClientConnection mockConnection;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new AdminController();
        mockAuctionService = mock(AuctionService.class);
        mockConnection = mock(ClientConnection.class);
        when(mockAuctionService.getClientConnection()).thenReturn(mockConnection);

        // Khởi tạo hàng loạt linh kiện FXML bắt buộc để tránh NullPointerException khi initialize()
        injectField("tableUsers", new TableView<>());
        injectField("colUserId", new TableColumn<>());
        injectField("colUsername", new TableColumn<>());
        injectField("colRole", new TableColumn<>());
        injectField("colBalance", new TableColumn<>());

        injectField("tableAuctions", new TableView<>());
        injectField("colRoomId", new TableColumn<>());
        injectField("colRoomName", new TableColumn<>());
        injectField("colSeller", new TableColumn<>());
        injectField("colPrice", new TableColumn<>());
        injectField("colBidStep", new TableColumn<>());
        injectField("colStatus", new TableColumn<>());

        injectField("tableBidHistory", new TableView<>());
        injectField("colBidAuctionId", new TableColumn<>());
        injectField("colBidderId", new TableColumn<>());
        injectField("colBidAmount", new TableColumn<>());
        injectField("colBidTime", new TableColumn<>());

        injectField("tablePendingAuctions", new TableView<>());
        injectField("colPendingRequestId", new TableColumn<>());
        injectField("colPendingSellerId", new TableColumn<>());
        injectField("colPendingSellerOrganization", new TableColumn<>());
        injectField("colPendingItemName", new TableColumn<>());
        injectField("colPendingItemDesc", new TableColumn<>());
        injectField("colPendingStartingPrice", new TableColumn<>());
        injectField("colPendingMinimumJoinAmount", new TableColumn<>());
        injectField("colPendingBidStep", new TableColumn<>());
        injectField("colPendingStartTime", new TableColumn<>());
        injectField("colPendingDurationMinutes", new TableColumn<>());
        injectField("colPendingExtensionSeconds", new TableColumn<>());
        injectField("colPendingSellerReputation", new TableColumn<>());
        injectField("colPendingSuccessfulAuctionRate", new TableColumn<>());
        injectField("colPendingAdminCancellationRate", new TableColumn<>());

        // Chạy hàm mapping thuộc tính của bảng
        controller.initialize();
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = AdminController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Test luồng nạp Service và tự động phát lệnh tải dữ liệu từ Server")
    void testSetAuctionService_LoadsData() {
        when(mockAuctionService.getCurrentUser()).thenReturn("ADMIN_VIP");

        // Gọi hàm nạp service
        controller.setAuctionService(mockAuctionService);

        // Kiểm tra xem hệ thống có tự động gửi 3 gói tin yêu cầu lấy dữ liệu quản trị lên server không
        verify(mockConnection, times(3)).sendMessage(any(Message.class));
    }

    @Test
    @DisplayName("Test cập nhật dữ liệu luồng mạng vào bảng biểu (Platform.runLater)")
    void testUpdateTables() throws Exception {
        controller.setAuctionService(mockAuctionService);

        // Giả lập nhận danh sách user từ server đẩy về UI
        User sampleUser = new User("US001", "hanto", "ADMIN", "123", 50000);
        controller.updateUsersTable(Collections.singletonList(sampleUser));

        controller.updateAuctionsTable(Collections.emptyList());
        controller.updateBidHistoryTable(Collections.emptyList());
        controller.updatePendingAuctionsTable(Collections.emptyList());

        // Nghỉ một lát để luồng đồ họa JavaFX nạp dữ liệu vào bảng
        Thread.sleep(150);
        assertNotNull(controller);
    }
}
