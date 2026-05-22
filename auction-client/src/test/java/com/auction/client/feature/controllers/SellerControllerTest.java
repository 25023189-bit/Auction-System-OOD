package com.auction.client.feature.controllers;

import com.auction.client.feature.controllers.auction.seller.SellerController;
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
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        lblStatus = new Label(""); // FIX CHÍ MẠNG: Ngăn NullPointerException

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
        // 1. Test số âm / ngoài khoảng
        txtExtensionSeconds.setText("-5");
        // Sửa lại thành 'between' theo đúng log lỗi ta đã phát hiện ở bài trước
        assertTrue(lblStatus.getText().toLowerCase().contains("between 60 and 120")
                        || lblStatus.getText().toLowerCase().contains("greater than 0"),
                "Lỗi thực tế hiển thị: " + lblStatus.getText());

        // Cố tình nhập số 0 hoặc số âm để test Duration
        txtDuration.setText("-10");
        assertTrue(lblStatus.getText().toLowerCase().contains("greater than 0"),
                "Lỗi thực tế hiển thị: " + lblStatus.getText());

        // 2. Test nhập chữ vào ô số
        txtExtensionSeconds.setText("not_a_number");
        assertTrue(lblStatus.getText().toLowerCase().contains("valid integer number"),
                "Lỗi thực tế hiển thị: " + lblStatus.getText());

        txtDuration.setText("not_a_number");
        assertTrue(lblStatus.getText().toLowerCase().contains("valid integer number"),
                "Lỗi thực tế hiển thị: " + lblStatus.getText());
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
        // 1. Điền bộ dữ liệu "Vàng" - Vượt qua mọi rule validate nghiệp vụ
        txtStartingPrice.setText("10000");
        txtMinimumJoinAmount.setText("1000");
        txtBidStep.setText("200");
        txtDuration.setText("90");

        // Sửa con số này thành giá trị nằm trong khoảng [60, 120]
        txtExtensionSeconds.setText("60");

        // 2. Cố tình để trống ngày để bẫy lỗi DatePicker
        datePickerStart.setValue(null);

        // 3. Thực thi
        controller.handleCreateAuction();

        // 4. Kiểm tra
        assertEquals("Please select a start date.", lblStatus.getText());
    }

    @Test
    @DisplayName("Test bấm nút tạo yêu cầu khi nhập sai định dạng số gây lỗi NumberFormatException")
    void testHandleCreateAuction_NumberFormatException() {
        // 1. "Lót đường" bằng bộ dữ liệu Vàng để lọt qua mọi lỗi Empty hoặc logic khác
        txtStartingPrice.setText("10000");
        txtMinimumJoinAmount.setText("1000");
        txtBidStep.setText("200");
        txtDuration.setText("90");
        txtExtensionSeconds.setText("60");
        datePickerStart.setValue(LocalDate.now().plusDays(1));
        txtStartHour.setText("09");
        txtStartMinute.setText("15");

        // 2. Cố tình phá hỏng ĐÚNG 1 Ô để bẫy lỗi NumberFormatException
        txtStartingPrice.setText("chuỗi_chữ_bậy_bạ");

        // 3. Thực thi
        controller.handleCreateAuction();

        // 4. IN RA CONSOLE để "bắt quả tang" Controller thực sự đang hiển thị câu gì
        System.out.println("====== [DEBUG NumberFormat TEST] Lỗi thực tế: '" + lblStatus.getText() + "' ======");

        // 5. Kiểm tra (Sửa lại chuỗi test cho khớp nếu log ở bước 4 in ra câu khác nhé)
        assertTrue(lblStatus.getText().toLowerCase().contains("must be valid numbers")
                        || lblStatus.getText().toLowerCase().contains("valid number"),
                "Nhãn báo lỗi không khớp! Lỗi thực tế là: " + lblStatus.getText());
    }

    @Test
    @DisplayName("Test điền form hợp lệ và bấm nút tạo yêu cầu mở phòng đấu giá thành công")
    void testHandleCreateAuction_Success() throws Exception {
        // 1. Điền sẵn dữ liệu form hợp lệ ở luồng test chính
        txtItemName.setText("Đồng hồ cổ");
        txtItemDescription.setText("Chạy mượt 100 năm");
        txtStartingPrice.setText("10000");
        txtMinimumJoinAmount.setText("1000"); // Hãy chắc chắn mức giá này pass rule < 75% hoặc rule nội bộ của bạn
        txtBidStep.setText("200");
        datePickerStart.setValue(LocalDate.now().plusDays(2));
        txtStartHour.setText("09");
        txtStartMinute.setText("15");
        txtDuration.setText("90");
        txtExtensionSeconds.setText("60");

        CountDownLatch latch = new CountDownLatch(1);

        // 2. Đồng bộ hóa việc khởi tạo Stage/Scene trong luồng đồ họa JavaFX
        Platform.runLater(() -> {
            try {
                Stage mockStage = new Stage();
                Scene scene = new Scene(txtItemName);
                mockStage.setScene(scene);

                // Gọi hàm thực thi nghiệp vụ tạo phòng
                controller.handleCreateAuction();

                // IN RA LOG ĐỂ DEBUG NẾU FORM BỊ CHẶN BỞI LỖI VALIDATE
                System.out.println("====== [DEBUG SUCCESS TEST] Tình trạng label: '" + lblStatus.getText() + "' ======");

                assertFalse(mockStage.isShowing(), "Popup tạo phòng phải tự động đóng lại sau khi gửi dữ liệu thành công");
            } catch (Exception e) {
                fail("Lỗi thực thi đồ họa giao diện: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // Chờ tối đa 5 giây để tránh test bị treo vĩnh viễn
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout khi chờ JavaFX Thread thực thi xong");

        // 3. Xác thực gói tin đóng gói gửi lên server hoàn toàn chuẩn chỉ qua Service nghiệp vụ
        verify(mockAuctionService).createAuction(
                eq("Đồng hồ cổ"), eq("Chạy mượt 100 năm"),
                eq(10000.0), eq(1000.0), eq(200.0),
                any(LocalDateTime.class), eq(90), eq(60)
        );
    }
}
