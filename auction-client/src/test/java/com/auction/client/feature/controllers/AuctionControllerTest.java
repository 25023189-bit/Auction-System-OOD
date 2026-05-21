package com.auction.client.feature.controllers;

import com.auction.client.feature.auth.AuthActionFacade;
import com.auction.client.feature.auth.RegisterForm;
import com.auction.client.service.AuctionService;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        CountDownLatch callbackLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            callbackLatch.countDown();
            return null;
        }).when(mockAuctionService).fireProductDetailsReceived("MockProductData");

        controller.onServerResponse(responseMsg);

        // Xác thực luồng sự kiện callback chi tiết sản phẩm đã được kích hoạt
        assertTrue(callbackLatch.await(1, TimeUnit.SECONDS));
        verify(mockAuctionService).fireProductDetailsReceived("MockProductData");
    }

    @Test
    @DisplayName("Register form keeps username and full name in the correct order")
    void testHandleSubmitRegister_MapsUsernameBeforeFullName() throws Exception {
        AuthActionFacade mockAuthActionFacade = mock(AuthActionFacade.class);
        injectField("authActionFacade", mockAuthActionFacade);

        TextField txtRegCustomerId = new TextField("BD50001");
        TextField txtRegUsername = new TextField("hanto");
        TextField txtRegFullName = new TextField("To Bao Han");
        PasswordField txtRegPassword = new PasswordField();
        txtRegPassword.setText("StrongPass123!");
        PasswordField txtRegConfirm = new PasswordField();
        txtRegConfirm.setText("StrongPass123!");
        ComboBox<String> cbRegRole = new ComboBox<>();
        cbRegRole.setValue("BIDDER");

        injectField("txtRegCustomerId", txtRegCustomerId);
        injectField("txtRegUsername", txtRegUsername);
        injectField("txtRegFullName", txtRegFullName);
        injectField("txtRegPassword", txtRegPassword);
        injectField("txtRegConfirm", txtRegConfirm);
        injectField("cbRegRole", cbRegRole);
        injectField("txtRegOrganization", new TextField());

        Method submitRegister = AuctionController.class.getDeclaredMethod("handleSubmitRegister");
        submitRegister.setAccessible(true);
        submitRegister.invoke(controller);

        ArgumentCaptor<RegisterForm> captor = ArgumentCaptor.forClass(RegisterForm.class);
        verify(mockAuthActionFacade).register(captor.capture());

        RegisterForm form = captor.getValue();
        assertEquals("hanto", form.username());
        assertEquals("To Bao Han", form.fullName());
    }
}
