package com.auction.client.network.messaging;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.server.service.AuctionService;
import javafx.scene.control.Alert;

/**
 * Fallback handler cho các action luồng đấu giá dùng chung nhiều màn hình.
 *
 * Vai trò:
 * - Xử lý kết quả tạo auction, lỗi join/bid, đóng phiên và thông báo phiên kết thúc.
 * - Điều hướng về lobby, refresh room list và cập nhật presenter khi action không thuộc handler chính.
 *
 * Luồng chính:
 * 1. FallbackMessageHandler chuyển action được supports() vào handler.
 * 2. Handler show alert/cập nhật presenter/session/navigator rồi gọi AuctionService khi cần refresh.
 *
 * Business rules:
 * - ROOM_FAIL phải xóa room khỏi session, quay về lobby và tải lại danh sách phòng.
 * - AUCTION_CLOSED_NOTIFY chỉ tác động nếu roomId trong message khớp room hiện tại.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác Alert, presenter, session và navigator trên luồng UI.
 * - Dependency: MessageHandler, AuctionService, SessionStore, SceneNavigator, LobbyUserInfoBinder, AuctionRoomPresenter, Alert.
 */
public class AuctionFlowFallbackHandler implements MessageHandler {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final SceneNavigator sceneNavigator;
    private final LobbyUserInfoBinder lobbyUserInfoBinder;
    private final AuctionRoomPresenter auctionRoomPresenter;

    public AuctionFlowFallbackHandler(AuctionService auctionService,
                                      SessionStore sessionStore,
                                      SceneNavigator sceneNavigator,
                                      LobbyUserInfoBinder lobbyUserInfoBinder,
                                      AuctionRoomPresenter auctionRoomPresenter) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.lobbyUserInfoBinder = lobbyUserInfoBinder;
        this.auctionRoomPresenter = auctionRoomPresenter;
    }

    @Override
    public boolean supports(String action) {
        // Nhóm action này bao gồm tạo phòng, lỗi vào phòng, lỗi bid và thông báo đóng phiên.
        return switch (action) {
            case "CREATE_AUCTION_SUCCESS",
                 "CREATE_AUCTION_PENDING",
                 "CREATE_AUCTION_FAIL",
                 "UPDATE_ROOMS",
                 "BID_FAIL",
                 "ROOM_FAIL",
                 "BID_SUCCESS",
                 "CLOSE_AUCTION_SUCCESS",
                 "CLOSE_AUCTION_FAIL",
                 "AUCTION_CLOSED_NOTIFY" -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Message msg) {
        switch (msg.getAction()) {
            case "CREATE_AUCTION_SUCCESS" -> {
                sessionStore.setCurrentRoomId(msg.id);
                auctionService.joinRoom(msg.id);
            }
            case "CREATE_AUCTION_PENDING" -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, String.valueOf(msg.getData()));
                alert.setHeaderText("Auction Waiting For Approval");
                alert.showAndWait();
            }
            case "CREATE_AUCTION_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Create Auction");
                alert.showAndWait();
            }
            case "UPDATE_ROOMS" -> auctionService.getRooms();
            case "BID_FAIL" -> {
                if (auctionRoomPresenter != null) {
                    auctionRoomPresenter.appendChat("Error: " + msg.getData());
                }
            }
            case "ROOM_FAIL" -> {
                // Nếu vào phòng thất bại, quay lại lobby và tải lại danh sách phòng mới nhất.
                Alert alert = new Alert(Alert.AlertType.WARNING, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Join Room");
                alert.showAndWait();
                sessionStore.setCurrentRoom(null);
                sessionStore.setCurrentRoomId(null);
                sceneNavigator.showLobby();
                if (lobbyUserInfoBinder != null) {
                    lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
                }
                auctionService.getRooms();
            }
            case "BID_SUCCESS" -> {
                if (auctionRoomPresenter != null && msg.getData() instanceof Double price) {
                    auctionRoomPresenter.showCurrentPrice(price, null);
                }
            }
            case "CLOSE_AUCTION_SUCCESS" -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The auction was closed by the seller.");
                alert.setHeaderText("Auction Closed");
                alert.showAndWait();
            }
            case "CLOSE_AUCTION_FAIL" -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, String.valueOf(msg.getData()));
                alert.setHeaderText("Unable to Close Auction");
                alert.showAndWait();
            }
            case "AUCTION_CLOSED_NOTIFY" -> {
                String currentRoomId = sessionStore.getCurrentRoomId();
                if (currentRoomId != null && currentRoomId.equals(String.valueOf(msg.getData()))) {
                    // Chỉ user đang ở đúng phòng bị đóng mới bị đưa về lobby.
                    Alert alert = new Alert(Alert.AlertType.WARNING, "The auction has ended. You will be returned to the main lobby.");
                    alert.setHeaderText("Auction Ended");
                    alert.showAndWait();
                    sessionStore.setCurrentRoom(null);
                    sessionStore.setCurrentRoomId(null);
                    sceneNavigator.showLobby();
                    if (lobbyUserInfoBinder != null) {
                        lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
                    }
                    auctionService.getRooms();
                }
            }
        }
    }
}
