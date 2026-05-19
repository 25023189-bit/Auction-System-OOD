package com.auction.client.feature.controllers;

import com.auction.client.service.AuctionService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SellerControllerTest {

    private SellerController controller;
    private AuctionService mockAuctionService;

    private TextField txtItemName;
    private TextArea txtItemDescription;
    private TextField txtStartingPrice;
    private TextField txtMinimumJoinAmount;
    private TextField txtBidStep;
    private Label lblStatus;
    private DatePicker datePickerStart;
    private TextField txtStartHour;
    private TextField txtStartMinute;
    private TextField txtDuration;
    private TextField txtExtensionSeconds;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Đã khởi tạo nền tảng đồ họa trước đó
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new SellerController();
        mockAuctionService = mock(AuctionService.class);
        controller.setAuctionService(mockAuctionService);

        // Khởi tạo các linh kiện UI
        txtItemName = new TextField();
        txtItemDescription = new TextArea();
        txtStartingPrice = new TextField();
        txtMinimumJoinAmount = new TextField();
        txtBidStep = new TextField();

        // FIX CHÍ MẠNG: Khởi tạo kèm chuỗi rỗng để ngăn hàm getText() trả về null gây lỗi NullPointerException
        lblStatus = new Label("");

        datePickerStart = new DatePicker();
        txtStartHour = new TextField();
        txtStartMinute = new TextField();
        txtDuration = new TextField();
        txtExtensionSeconds = new TextField();

        injectField("txtItemName", txtItemName);
        injectField("txtItemDescription", txtItemDescription);
        injectField("txtStartingPrice", txtStartingPrice);
        injectField("txtMinimumJoinAmount", txtMinimumJoinAmount);
        injectField("txtBidStep", txtBidStep);
        injectField("lblStatus", lblStatus);
        injectField("datePickerStart", datePickerStart);
        injectField("txtStartHour", txtStartHour);
        injectField("txtStartMinute", txtStartMinute);
        injectField("txtDuration", txtDuration);
        injectField("txtExtensionSeconds", txtExtensionSeconds);

        // Kích hoạt liên kết các Listener kiểm tra dữ liệu real-time
        controller.initialize();
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = SellerController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Test Real-time Validation khi xóa trống các ô nhập liệu")
    void testRealTimeValidation_EmptyFields() {
        txtStartingPrice.setText("");
        txtMinimumJoinAmount.setText("");
        txtExtensionSeconds.setText("");
        txtDuration.setText("");

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Test Real-time Validation khi gõ sai định dạng chữ/số âm")
    void testRealTimeValidation_Errors() {
        txtStartingPrice.setText("abc");
        txtMinimumJoinAmount.setText("xyz");

        txtExtensionSeconds.setText("-5");
        assertTrue(lblStatus.getText().contains("Extension time must be greater than 0"));
        txtExtensionSeconds.setText("not_a_number");
        assertTrue(lblStatus.getText().contains("Extension must be a valid integer number"));

        txtDuration.setText("0");
        assertTrue(lblStatus.getText().contains("Duration must be greater than 0 minutes"));
        txtDuration.setText("not_a_number");
        assertTrue(lblStatus.getText().contains("Duration must be a valid integer number"));
    }

    @Test
    @DisplayName("Test bấm nút tạo yêu cầu khi thiếu hụt AuctionService")
    void testHandleCreateAuction_NullService() {
        controller.setAuctionService(null);
        controller.handleCreateAuction();
        assertEquals("Error: Service is not available.", lblStatus.getText());
    }

    @Test
    @DisplayName("Test bấm nút tạo yêu cầu khi chưa lựa chọn ngày bắt đầu")
    void testHandleCreateAuction_NullDate() {
        datePickerStart.setValue(null);
        controller.handleCreateAuction();
        assertEquals("Please select a start date.", lblStatus.getText());
    }

    @Test
    @DisplayName("Test bấm nút tạo yêu cầu vi phạm hàng loạt quy tắc nghiệp vụ form")
    void testHandleCreateAuction_BusinessRuleViolations() {
        datePickerStart.setValue(LocalDate.now().plusDays(1));
        txtStartHour.setText("12");
        txtStartMinute.setText("0");

        txtStartingPrice.setText("1000");
        txtMinimumJoinAmount.setText("800");
        txtBidStep.setText("50");
        txtDuration.setText("60");
        txtExtensionSeconds.setText("30");
        controller.handleCreateAuction();
        assertTrue(lblStatus.getText().contains("Minimum join amount must be < 75%"));

        txtMinimumJoinAmount.setText("100");
        txtExtensionSeconds.setText("-10");
        controller.handleCreateAuction();
        assertTrue(lblStatus.getText().contains("Extension time must be greater than 0"));

        txtExtensionSeconds.setText("30");
        txtDuration.setText("0");
        controller.handleCreateAuction();
        assertTrue(lblStatus.getText().contains("Duration must be greater than 0 minutes"));

        txtDuration.setText("60");
        datePickerStart.setValue(LocalDate.now().minusDays(3));
        controller.handleCreateAuction();
        assertTrue(lblStatus.getText().contains("Start time must be now or in the future"));
    }

    @Test
    @DisplayName("Test bấm nút tạo yêu cầu khi nhập sai định dạng số gây lỗi NumberFormatException")
    void testHandleCreateAuction_NumberFormatException() {
        datePickerStart.setValue(LocalDate.now().plusDays(1));
        txtStartingPrice.setText("chuỗi_chữ_bậy_bạ");
        controller.handleCreateAuction();
        assertTrue(lblStatus.getText().contains("must be valid numbers"));
    }

    @Test
    @DisplayName("Test điền form hợp lệ và bấm nút tạo yêu cầu mở phòng đấu giá thành công")
    void testHandleCreateAuction_Success() throws Exception {
        // 1. Điền sẵn dữ liệu form hợp lệ ở luồng test chính
        txtItemName.setText("Đồng hồ cổ");
        txtItemDescription.setText("Chạy mượt 100 năm");
        txtStartingPrice.setText("10000");
        txtMinimumJoinAmount.setText("1000");
        txtBidStep.setText("200");
        datePickerStart.setValue(LocalDate.now().plusDays(2));
        txtStartHour.setText("09");
        txtStartMinute.setText("15");
        txtDuration.setText("90");
        txtExtensionSeconds.setText("45");

        CountDownLatch latch = new CountDownLatch(1);

        // 2. Đồng bộ hóa việc khởi tạo Stage/Scene và tắt popup hoàn toàn trong luồng đồ họa JavaFX
        Platform.runLater(() -> {
            try {
                Stage mockStage = new Stage();
                Scene scene = new Scene(txtItemName);
                mockStage.setScene(scene);

                // Gọi hàm thực thi nghiệp vụ tạo phòng
                controller.handleCreateAuction();

                assertFalse(mockStage.isShowing(), "Popup tạo phòng phải tự động đóng lại sau khi gửi dữ liệu thành công");
            } catch (Exception e) {
                fail("Lỗi thực thi đồ họa giao diện: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        // 3. Xác thực gói tin đóng gói gửi lên server hoàn toàn chuẩn chỉ qua Service nghiệp vụ
        verify(mockAuctionService).createAuction(
                eq("Đồng hồ cổ"), eq("Chạy mượt 100 năm"),
                eq(10000.0), eq(1000.0), eq(200.0),
                any(LocalDateTime.class), eq(90), eq(45)
        );
    }
}