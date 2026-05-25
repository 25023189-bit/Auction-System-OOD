package com.auction.client.shared.support;

import javafx.scene.control.Alert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class FxAlertServiceTest {

    // Vẫn phải bật "động cơ" JavaFX ngầm để Mockito không bị lỗi Toolkit khi làm giả Alert
    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {}
    }

    private FxAlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new FxAlertService();
    }

    @Test
    @DisplayName("Hàm show() phải khởi tạo Alert và gọi showAndWait()")
    void testShow_CreatesAlertAndShowsIt() {
        // Dùng MockedConstruction để "bắt cóc" lệnh new Alert() ngay khi nó vừa được gọi
        try (MockedConstruction<Alert> mockedAlerts = Mockito.mockConstruction(Alert.class)) {

            // Chạy hàm thật
            alertService.show(Alert.AlertType.CONFIRMATION, "Tiêu đề", "Đầu mục", "Nội dung");

            // 1. Xác thực chỉ có đúng 1 đối tượng Alert được tạo ra
            assertEquals(1, mockedAlerts.constructed().size());

            // 2. Lấy cái Alert giả vừa bị bắt cóc ra để kiểm tra
            Alert mockAlert = mockedAlerts.constructed().get(0);

            // 3. Xác thực dữ liệu đã được nhồi vào đúng chỗ chưa
            verify(mockAlert).setTitle("Tiêu đề");
            verify(mockAlert).setHeaderText("Đầu mục");
            verify(mockAlert).setContentText("Nội dung");

            // 4. Quan trọng nhất: Đảm bảo có gọi lệnh hiển thị
            verify(mockAlert).showAndWait();
        }
    }

    @Test
    @DisplayName("Shortcut info() phải map đúng loại INFORMATION và header null")
    void testInfoShortcut_MapsCorrectly() {
        try (MockedConstruction<Alert> mockedAlerts = Mockito.mockConstruction(Alert.class)) {
            alertService.info("Thông tin", "Đăng ký thành công");

            Alert mockAlert = mockedAlerts.constructed().get(0);
            verify(mockAlert).setTitle("Thông tin");
            verify(mockAlert).setHeaderText(null); // Header của INFO phải là null theo thiết kế
            verify(mockAlert).setContentText("Đăng ký thành công");
            verify(mockAlert).showAndWait();
        }
    }

    @Test
    @DisplayName("Shortcut warning() phải map đúng loại WARNING")
    void testWarningShortcut_MapsCorrectly() {
        try (MockedConstruction<Alert> mockedAlerts = Mockito.mockConstruction(Alert.class)) {
            alertService.warning("Cảnh báo", "Sắp hết giờ");

            Alert mockAlert = mockedAlerts.constructed().get(0);
            verify(mockAlert).setTitle("Cảnh báo");
            verify(mockAlert).setHeaderText(null);
            verify(mockAlert).setContentText("Sắp hết giờ");
            verify(mockAlert).showAndWait();
        }
    }

    @Test
    @DisplayName("Shortcut error() phải map đúng loại ERROR")
    void testErrorShortcut_MapsCorrectly() {
        try (MockedConstruction<Alert> mockedAlerts = Mockito.mockConstruction(Alert.class)) {
            alertService.error("Lỗi", "Mất kết nối");

            Alert mockAlert = mockedAlerts.constructed().get(0);
            verify(mockAlert).setTitle("Lỗi");
            verify(mockAlert).setHeaderText(null);
            verify(mockAlert).setContentText("Mất kết nối");
            verify(mockAlert).showAndWait();
        }
    }
}