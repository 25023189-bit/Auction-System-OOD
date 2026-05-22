package com.auction.client.shared.support;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DefaultUiResetServiceTest {

    // === ĐOẠN CODE "THẦN THÁNH" CHỮA LỖI TOOLKIT ===
    @BeforeAll
    static void initJFX() {
        try {
            // Lén bật động cơ JavaFX chạy ngầm để không bị lỗi Toolkit khi Mock
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Nếu động cơ đã bật rồi thì bỏ qua
        }
    }
    // ===============================================

    private DefaultUiResetService uiResetService;

    // Khai báo các đối tượng giao diện giả (Mocks)
    private Button mockBtnCreateAuction;
    private Button mockBtnCloseAuction;
    private Button mockBtnPlaceBid;
    private TextField mockTxtBidAmount;
    private TextField mockTxtChatInput;
    private TextArea mockTxtChatLog;
    private Label mockLblUsername;
    private Label mockLblBalance;
    private Label mockLblAuctionItemName;
    private Label mockLblCurrentPrice;
    private Label mockLblTimer;

    @BeforeEach
    void setUp() {
        // Khởi tạo đồ giả bằng Mockito
        mockBtnCreateAuction = mock(Button.class);
        mockBtnCloseAuction = mock(Button.class);
        mockBtnPlaceBid = mock(Button.class);
        mockTxtBidAmount = mock(TextField.class);
        mockTxtChatInput = mock(TextField.class);
        mockTxtChatLog = mock(TextArea.class);
        mockLblUsername = mock(Label.class);
        mockLblBalance = mock(Label.class);
        mockLblAuctionItemName = mock(Label.class);
        mockLblCurrentPrice = mock(Label.class);
        mockLblTimer = mock(Label.class);

        // Bơm đồ giả vào Service
        uiResetService = new DefaultUiResetService(
                mockBtnCreateAuction, mockBtnCloseAuction, mockBtnPlaceBid,
                mockTxtBidAmount, mockTxtChatInput, mockTxtChatLog,
                mockLblUsername, mockLblBalance, mockLblAuctionItemName,
                mockLblCurrentPrice, mockLblTimer
        );
    }

    @Test
    @DisplayName("Kiểm tra luồng dọn dẹp bình thường: Các lệnh UI phải được gọi chính xác")
    void testResetSessionUi_WithValidControls_ResetsProperly() {
        // Thực thi hàm dọn dẹp
        uiResetService.resetSessionUi();

        // 1. Xác thực các nút quyền hạn (Role-based) đã bị ẩn đi
        verify(mockBtnCreateAuction, times(1)).setVisible(false);
        verify(mockBtnCreateAuction, times(1)).setManaged(false);
        verify(mockBtnCloseAuction, times(1)).setVisible(false);
        verify(mockBtnCloseAuction, times(1)).setManaged(false);

        // 2. Xác thực nút Đặt giá và các ô nhập liệu đã được mở khóa và xóa trắng
        verify(mockBtnPlaceBid, times(1)).setDisable(false);
        verify(mockTxtBidAmount, times(1)).clear();
        verify(mockTxtBidAmount, times(1)).setDisable(false);
        verify(mockTxtChatInput, times(1)).clear();
        verify(mockTxtChatInput, times(1)).setDisable(false);

        // 3. Xác thực lịch sử Chat và các nhãn (Label) đã bị xóa thông tin cũ
        verify(mockTxtChatLog, times(1)).clear();
        verify(mockLblUsername, times(1)).setText("");
        verify(mockLblBalance, times(1)).setText("");
        verify(mockLblAuctionItemName, times(1)).setText("");
        verify(mockLblCurrentPrice, times(1)).setText("");
        verify(mockLblTimer, times(1)).setText("");
    }

    @Test
    @DisplayName("Kiểm tra sức chịu đựng (Edge Case): Truyền toàn null không được văng lỗi")
    void testResetSessionUi_WithNullControls_DoesNotThrowException() {
        // Cố tình tạo một Service mà truyền null vào toàn bộ các nút
        DefaultUiResetService nullService = new DefaultUiResetService(
                null, null, null, null, null, null, null, null, null, null, null
        );

        // Xác thực hệ thống vẫn chạy êm ru nhờ các lệnh kiểm tra (if control != null)
        assertDoesNotThrow(() -> {
            nullService.resetSessionUi();
        }, "Hàm resetSessionUi phải an toàn, không được ném lỗi NullPointerException khi các control bị rỗng");
    }
}