package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Renderer chuyển danh sách phòng lobby đã chuẩn hóa thành card trong FlowPane.
 *
 * Vai trò:
 * - Render lại toàn bộ danh sách LobbyRoomDisplayModel thành card.
 * - Cập nhật nhanh giá của một card khi nhận UPDATE_PRICE.
 *
 * Luồng chính:
 * 1. AdvancedLobbyMessageHandler gọi render(models) sau ROOM_LIST.
 * 2. Khi có UPDATE_PRICE, handler gọi updatePrice(roomId, newPrice) để sửa label trên card hiện có.
 *
 * Business rules:
 * - render() phải clear danh sách cũ trước khi thêm card mới.
 * - updatePrice() chỉ sửa card có userData khớp roomId.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: FlowPane/Node JavaFX phải cập nhật trên JavaFX Application Thread.
 * - Dependency: FlowPane, DefaultAuctionCardFactory, LobbyRoomDisplayModel, VBox, Label.
 */
public class LobbyRoomListRenderer {
    private final FlowPane paneSelectAuction;
    private final DefaultAuctionCardFactory cardFactory;

    public LobbyRoomListRenderer(FlowPane paneSelectAuction,
                                 DefaultAuctionCardFactory cardFactory) {
        this.paneSelectAuction = paneSelectAuction;
        this.cardFactory = cardFactory;
    }

    // Render lại danh sách từ đầu khi server gửi ROOM_LIST mới.
    public void render(List<LobbyRoomDisplayModel> models) {
        if (paneSelectAuction == null) return;

        paneSelectAuction.getChildren().clear();
        if (models == null) return;

        for (LobbyRoomDisplayModel model : models) {
            VBox card = cardFactory.createDefault(model);
            card.setUserData(model.getRoomId());
            applyBidderLobbyCardLayout(card);
            paneSelectAuction.getChildren().add(card);
        }
    }

    private void applyBidderLobbyCardLayout(VBox card) {
        boolean supportedGrid = paneSelectAuction.getStyleClass().contains("bidder-auction-grid")
                || paneSelectAuction.getStyleClass().contains("seller-auction-grid");
        if (!supportedGrid) return;

        card.setMinSize(200, 286);
        card.setPrefSize(200, 286);
        card.setMaxSize(200, 286);
    }

    // Tìm đúng card bằng roomId được lưu trong userData và cập nhật label giá.
    public void updatePrice(String roomId, double newPrice) {
        if (paneSelectAuction == null || roomId == null || roomId.isBlank()) return;

        for (Node node : paneSelectAuction.getChildren()) {
            if (!(node instanceof VBox card)) continue;

            Object userData = card.getUserData();
            if (userData == null || !roomId.equals(userData.toString())) continue;

            for (Node child : card.getChildren()) {
                if (child instanceof Label label) {
                    String text = label.getText();
                    if (text != null && (text.startsWith("Current Price:") || text.startsWith("Price:"))) {
                        label.setText("Price: " + newPrice);
                        return;
                    }
                }
            }
        }
    }
}
