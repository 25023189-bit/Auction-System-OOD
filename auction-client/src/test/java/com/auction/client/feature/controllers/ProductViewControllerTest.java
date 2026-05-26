package com.auction.client.feature.controllers;

import com.auction.client.feature.controllers.assistant.product.ProductViewController;
import com.auction.client.service.AuctionService;
import com.auction.common.model.AuctionRoom;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductViewControllerTest {

    private ProductViewController controller;
    private AuctionService mockAuctionService;
    private Label lblProductName;
    private Label lblDescription;
    private Label lblCurrentPrice;
    private Label lblBidCount;
    private Label lblTimeRemaining;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Đã khởi tạo từ trước
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        controller = new ProductViewController();
        mockAuctionService = mock(AuctionService.class);

        lblProductName = new Label();
        lblDescription = new Label();
        lblCurrentPrice = new Label();
        lblBidCount = new Label();
        lblTimeRemaining = new Label();

        injectField("lblProductName", lblProductName);
        injectField("lblDescription", lblDescription);
        injectField("lblCurrentPrice", lblCurrentPrice);
        injectField("lblBidCount", lblBidCount);
        injectField("lblTimeRemaining", lblTimeRemaining);
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = ProductViewController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Test setRoomId hiển thị trạng thái Loading và gọi request lên Server")
    void testSetRoomId() {
        controller.setAuctionService(mockAuctionService);
        controller.setRoomId("AU10001");

        assertEquals("Loading data from server...", lblDescription.getText());
        verify(mockAuctionService).requestProductDetails("AU10001");
    }

    @Test
    void setRoomIdWithoutServiceStillShowsLoading() {
        controller.setRoomId("AU10001");

        assertEquals("Loading data from server...", lblDescription.getText());
        verifyNoInteractions(mockAuctionService);
    }

    @Test
    @DisplayName("Test nhận thông tin phòng thành công từ luồng mạng")
    void testProductDetailsCallback_Success() throws Exception {
        ArgumentCaptor<Consumer<Object>> callbackCaptor = ArgumentCaptor.forClass(Consumer.class);
        controller.setAuctionService(mockAuctionService);
        verify(mockAuctionService).setProductDetailsCallback(callbackCaptor.capture());

        Consumer<Object> callback = callbackCaptor.getValue();
        AuctionRoom mockRoom = new AuctionRoom("AU10001", "Bình cổ triều Nguyễn", 50000.0, "SELLER_99");
        mockRoom.setItemDescription("Đồ cổ nguyên bản cực hiếm");

        // Kích hoạt callback nghiệp vụ mạng
        callback.accept(mockRoom);

        // BÀI TỦ ĐỒ HỌA: Dùng CountDownLatch đồng bộ luồng tuyệt đối, triệt tiêu hoàn toàn độ trễ luồng
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await();

        assertEquals("Bình cổ triều Nguyễn", lblProductName.getText());
        assertEquals("Đồ cổ nguyên bản cực hiếm", lblDescription.getText());

        // SỬA LỖI LOCALE: Chấp nhận cả 50,000 $ hoặc 50.000 $ tùy máy chạy test
        String priceText = lblCurrentPrice.getText();
        assertNotNull(priceText);
        assertEquals("0", lblBidCount.getText());
        assertEquals("Ended", lblTimeRemaining.getText());
        assertTrue(priceText.matches("50[.,]000 \\$"), "Giá hiển thị thực tế là: " + priceText);
    }

    @Test
    @DisplayName("Test kịch bản Server báo lỗi không tìm thấy thông tin chi tiết")
    void testProductDetailsCallback_NullData() throws Exception {
        ArgumentCaptor<Consumer<Object>> callbackCaptor = ArgumentCaptor.forClass(Consumer.class);
        controller.setAuctionService(mockAuctionService);
        verify(mockAuctionService).setProductDetailsCallback(callbackCaptor.capture());

        Consumer<Object> callback = callbackCaptor.getValue();
        callback.accept(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await();

        assertEquals("Product information not found!", lblDescription.getText());
    }

    @Test
    @DisplayName("Test kịch bản dữ liệu trả về lỗi cấu trúc kích hoạt khối catch(Exception e)")
    void testProductDetailsCallback_CastException() throws Exception {
        ArgumentCaptor<Consumer<Object>> callbackCaptor = ArgumentCaptor.forClass(Consumer.class);
        controller.setAuctionService(mockAuctionService);
        verify(mockAuctionService).setProductDetailsCallback(callbackCaptor.capture());

        Consumer<Object> callback = callbackCaptor.getValue();
        callback.accept("Lỗi hệ thống nghiêm trọng từ Server!");

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await();

        assertEquals("Lỗi hệ thống nghiêm trọng từ Server!", lblDescription.getText());
    }
}
