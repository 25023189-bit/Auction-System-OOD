package com.auction.client.feature.lobby;

import com.auction.client.core.ui.ViewPresenter;
import com.auction.common.model.AuctionRoom;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Presenter đơn giản render danh sách phòng vào lobby.
 *
 * Vai trò:
 * - Xóa và render lại các card phòng trong FlowPane.
 * - Dùng CardFactory để tách việc tạo card khỏi presenter.
 *
 * Luồng chính:
 * 1. LobbyMessageHandler gọi showRooms(rooms) khi nhận ROOM_LIST.
 * 2. Presenter clear FlowPane rồi thêm card mới cho từng AuctionRoom.
 *
 * Business rules:
 * - Danh sách null chỉ clear UI, không ném lỗi.
 * - Mỗi room được render thành một card độc lập qua CardFactory.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: FlowPane JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: ViewPresenter, FlowPane, CardFactory<AuctionRoom, VBox>, AuctionRoom.
 */
public class LobbyPresenter implements ViewPresenter {
    private final FlowPane paneSelectAuction;
    private final CardFactory<AuctionRoom, VBox> cardFactory;

    public LobbyPresenter(FlowPane paneSelectAuction,
                          CardFactory<AuctionRoom, VBox> cardFactory) {
        this.paneSelectAuction = paneSelectAuction;
        this.cardFactory = cardFactory;
    }

    // Xóa danh sách cũ rồi render danh sách mới từ server.
    public void showRooms(List<AuctionRoom> rooms) {
        if (paneSelectAuction == null) return;

        paneSelectAuction.getChildren().clear();
        if (rooms == null) return;

        for (AuctionRoom room : rooms) {
            paneSelectAuction.getChildren().add(cardFactory.create(room));
        }
    }

    @Override
    public void clear() {
        if (paneSelectAuction != null) {
            paneSelectAuction.getChildren().clear();
        }
    }
}
