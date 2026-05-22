package com.auction.client.shared.support;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultFxThreadExecutorTest {

    private DefaultFxThreadExecutor executor;
    private Runnable mockAction;
    private MockedStatic<Platform> mockedPlatform;

    @BeforeEach
    void setUp() {
        executor = new DefaultFxThreadExecutor();
        // 1. Tạo một hành động (Runnable) giả
        mockAction = mock(Runnable.class);

        // 2. Kích hoạt "Vùng không gian ảo" để thao túng class Platform
        mockedPlatform = mockStatic(Platform.class);
    }

    @AfterEach
    void tearDown() {
        // CỰC KỲ QUAN TRỌNG: Phải đóng vùng không gian ảo lại sau mỗi bài test
        // Nếu không nó sẽ làm sập các bài test khác!
        mockedPlatform.close();
    }

    @Test
    @DisplayName("Nếu đang ở JavaFX Thread -> Chạy hàm trực tiếp ngay lập tức")
    void testExecute_WhenOnFxThread_RunsDirectly() {
        // Giả vờ báo cho hệ thống: "Anh em đang đứng ở Thread Giao diện nhé!"
        mockedPlatform.when(Platform::isFxApplicationThread).thenReturn(true);

        executor.execute(mockAction);

        // Xác thực hành động đã được CHẠY TRỰC TIẾP
        verify(mockAction, times(1)).run();

        // Xác thực KHÔNG có lệnh nào bị đẩy vào hàng chờ runLater
        mockedPlatform.verify(() -> Platform.runLater(any(Runnable.class)), never());
    }

    @Test
    @DisplayName("Nếu đang ở Thread ngầm (Mạng/Đọc file) -> Đẩy vào Platform.runLater")
    void testExecute_WhenNotOnFxThread_DelegatesToRunLater() {
        // Giả vờ báo cho hệ thống: "Đang ở luồng Socket ngầm, cấm chạm vào UI!"
        mockedPlatform.when(Platform::isFxApplicationThread).thenReturn(false);

        executor.execute(mockAction);

        // Xác thực hành động KHÔNG HỀ bị chạy trực tiếp (vì sẽ gây lỗi văng app)
        verify(mockAction, never()).run();

        // Xác thực hành động đã được gói gém cẩn thận ném vào Platform.runLater
        mockedPlatform.verify(() -> Platform.runLater(mockAction), times(1));
    }
}