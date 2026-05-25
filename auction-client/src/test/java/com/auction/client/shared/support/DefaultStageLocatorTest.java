package com.auction.client.shared.support;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DefaultStageLocatorTest {

    private DefaultStageLocator locator;
    private MockedStatic<Window> mockedWindow;

    @BeforeEach
    void setUp() {
        locator = new DefaultStageLocator();
        // Mở "không gian ảo" đóng băng class Window của JavaFX
        mockedWindow = mockStatic(Window.class);
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp không gian ảo sau mỗi bài test để không gây lỗi chéo
        mockedWindow.close();
    }

    @Test
    @DisplayName("Trả về null nếu hệ thống hoàn toàn không có cửa sổ nào")
    void testResolveMainStage_NoWindows_ReturnsNull() {
        // Giả lập hệ thống trả về danh sách rỗng
        ObservableList<Window> emptyList = FXCollections.observableArrayList();
        mockedWindow.when(Window::getWindows).thenReturn(emptyList);

        Stage result = locator.resolveMainStage();

        assertNull(result, "Phải trả về null vì danh sách cửa sổ đang trống");
    }

    @Test
    @DisplayName("Trả về null nếu có cửa sổ nhưng tất cả đều đang bị ẩn (isShowing = false)")
    void testResolveMainStage_NoShowingWindows_ReturnsNull() {
        // Tạo 2 cửa sổ giả và cấu hình cho chúng trạng thái ẨN
        Stage hiddenStage1 = mock(Stage.class);
        Stage hiddenStage2 = mock(Stage.class);
        when(hiddenStage1.isShowing()).thenReturn(false);
        when(hiddenStage2.isShowing()).thenReturn(false);

        // Nhét vào hệ thống
        ObservableList<Window> hiddenList = FXCollections.observableArrayList(hiddenStage1, hiddenStage2);
        mockedWindow.when(Window::getWindows).thenReturn(hiddenList);

        Stage result = locator.resolveMainStage();

        assertNull(result, "Phải trả về null vì không có cửa sổ nào đang được show");
    }

    @Test
    @DisplayName("Trả về đúng cửa sổ (Stage) ĐẦU TIÊN đang hiển thị")
    void testResolveMainStage_WithShowingWindow_ReturnsFirstShowingStage() {
        // Tạo 3 cửa sổ: 1 ẩn, 2 hiện
        Stage hiddenStage = mock(Stage.class);
        Stage visibleStage1 = mock(Stage.class);
        Stage visibleStage2 = mock(Stage.class);

        when(hiddenStage.isShowing()).thenReturn(false);
        when(visibleStage1.isShowing()).thenReturn(true);
        when(visibleStage2.isShowing()).thenReturn(true);

        // Xếp hàng: Ẩn -> Hiện 1 -> Hiện 2
        ObservableList<Window> mixedList = FXCollections.observableArrayList(hiddenStage, visibleStage1, visibleStage2);
        mockedWindow.when(Window::getWindows).thenReturn(mixedList);

        Stage result = locator.resolveMainStage();

        // Thuật toán findFirst() phải bỏ qua cái Ẩn và tóm đúng cái Hiện số 1
        assertEquals(visibleStage1, result, "Phải trả về cửa sổ đang hiển thị xuất hiện đầu tiên trong danh sách");
    }
}