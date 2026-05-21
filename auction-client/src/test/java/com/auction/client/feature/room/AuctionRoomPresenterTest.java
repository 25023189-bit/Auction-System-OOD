package com.auction.client.feature.room;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class AuctionRoomPresenterTest {
    private Label lblItemName, lblPrice, lblTimer, lblParticipant, lblDesc;
    private TextArea txtChat, txtDescDisplay;
    private Button btnClose, btnBid;
    private TextField txtBidAmount;
    private AuctionRoomPresenter presenter;

    // THUỐC GIẢI Ở ĐÂY: Bật môi trường đồ họa ảo trước khi chạy bất kỳ test nào
    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Bỏ qua lỗi nếu Toolkit đã vô tình được bật từ trước
        }
    }

    @BeforeEach
    void setUp() {
        lblItemName = mock(Label.class);
        lblPrice = mock(Label.class);
        lblTimer = mock(Label.class);
        lblParticipant = mock(Label.class);
        lblDesc = mock(Label.class);
        txtChat = mock(TextArea.class);
        txtDescDisplay = mock(TextArea.class);
        btnClose = mock(Button.class);
        btnBid = mock(Button.class);
        txtBidAmount = mock(TextField.class);

        presenter = new AuctionRoomPresenter(
                lblItemName, lblPrice, lblTimer, lblParticipant, lblDesc,
                txtChat, txtDescDisplay, btnClose, btnBid, txtBidAmount
        );
    }

    @Test
    @DisplayName("Test showRoomInfo cập nhật đúng thông tin cơ bản")
    void testShowRoomInfo() {
        presenter.showRoomInfo("Laptop UET", 1500.0, "Cũ như mới");

        verify(lblItemName).setText("Laptop UET");
        verify(lblPrice).setText(anyString());
        verify(txtDescDisplay).setText("Cũ như mới");
    }

    @Test
    @DisplayName("Test showRoomInfo khi description trống thì dùng text mặc định")
    void testShowRoomInfo_EmptyDescription() {
        presenter.showRoomInfo("Laptop", 1000.0, "");
        verify(txtDescDisplay).setText("No item description available.");
    }

    @Test
    @DisplayName("Test đếm số lượng người tham gia (chống số âm)")
    void testShowParticipantCount() {
        presenter.showParticipantCount(5);
        verify(lblParticipant).setText("Participants: 5");

        presenter.showParticipantCount(-2);
        verify(lblParticipant).setText("Participants: 0");
    }

    @Test
    @DisplayName("Test cập nhật giá mới và ghi log chat")
    void testShowCurrentPrice() {
        presenter.showCurrentPrice(2000.0, "Han");
        verify(lblPrice).setText(anyString());
        verify(txtChat).appendText(contains("Han is holding the price at"));
    }

    @Test
    @DisplayName("Test ẩn/hiện nút đóng phiên cho người sở hữu phòng")
    void testSetOwnerControlsVisible() {
        presenter.setOwnerControlsVisible(true);
        verify(btnClose).setVisible(true);
        verify(btnClose).setManaged(true);
    }

    @Test
    @DisplayName("Test khóa giao diện đặt giá và báo lý do vào chat")
    void testDisableBidUi() {
        presenter.disableBidUi("Phiên đấu giá đã kết thúc");

        verify(btnBid).setDisable(true);
        verify(txtBidAmount).setDisable(true);
        verify(txtChat).appendText("Phiên đấu giá đã kết thúc\n");
    }

    @Test
    @DisplayName("Test thay đổi text và màu của đồng hồ Timer")
    void testSetTimerText() {
        presenter.setTimerText("10:00", Color.RED);
        verify(lblTimer).setText("10:00");
        verify(lblTimer).setTextFill(Color.RED);
    }

    @Test
    @DisplayName("Test clear dọn dẹp log chat")
    void testClear() {
        presenter.clear();
        verify(txtChat).clear();
    }

    @Test
    @DisplayName("Test an toàn không sập khi toàn bộ UI Component bị null")
    void testNullSafety() {
        AuctionRoomPresenter nullPresenter = new AuctionRoomPresenter(
                null, null, null, null, null, null, null, null, null, null
        );

        assertDoesNotThrow(() -> nullPresenter.showRoomInfo("A", 1, "B"));
        assertDoesNotThrow(() -> nullPresenter.showParticipantCount(1));
        assertDoesNotThrow(() -> nullPresenter.showCurrentPrice(1, "A"));
        assertDoesNotThrow(() -> nullPresenter.setOwnerControlsVisible(true));
        assertDoesNotThrow(() -> nullPresenter.disableBidUi("Lỗi"));
        assertDoesNotThrow(() -> nullPresenter.setTimerText("1", Color.BLACK));
        assertDoesNotThrow(nullPresenter::clear);
    }
}