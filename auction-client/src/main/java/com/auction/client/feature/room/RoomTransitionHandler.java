package com.auction.client.feature.room;

import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.session.SessionStore;
import com.auction.client.service.AuctionService;

/**
 * Handler điều phối việc rời phòng đấu giá và quay về lobby.
 *
 * Vai trò:
 * - Dừng timer, báo server rời phòng và xóa room khỏi session.
 * - Điều hướng về lobby, bind lại user header và request danh sách phòng mới.
 *
 * Luồng chính:
 * 1. AuctionController gọi backToLobby() khi user bấm quay lại.
 * 2. Handler stop timer, leaveRoom(), clear session room, showLobby() và getRooms().
 *
 * Business rules:
 * - Timer phải dừng trước khi đổi màn hình để callback cũ không cập nhật UI sai.
 * - Sau khi về lobby phải tải lại ROOM_LIST để tránh danh sách cũ.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: thao tác session/navigator/UI service trên luồng UI.
 * - Dependency: AuctionService, SessionStore, SceneNavigator, AuctionTimer, LobbyUserInfoBinder.
 */
public class RoomTransitionHandler {
    private final AuctionService auctionService;
    private final SessionStore sessionStore;
    private final SceneNavigator sceneNavigator;
    private final AuctionTimer auctionTimer;
    private final LobbyUserInfoBinder lobbyUserInfoBinder;

    public RoomTransitionHandler(AuctionService auctionService,
                                 SessionStore sessionStore,
                                 SceneNavigator sceneNavigator,
                                 AuctionTimer auctionTimer,
                                 LobbyUserInfoBinder lobbyUserInfoBinder) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.auctionTimer = auctionTimer;
        this.lobbyUserInfoBinder = lobbyUserInfoBinder;
    }

    public void backToLobby() {
        // Dừng timer trước khi đổi màn hình để timeline cũ không tiếp tục cập nhật UI.
        if (auctionTimer != null) {
            auctionTimer.stop();
        }

        if (auctionService != null) {
            // Báo server rằng user đã rời phòng hiện tại.
            auctionService.leaveRoom();
        }

        if (sessionStore != null) {
            // Xóa room khỏi session để các response cũ không còn bind vào phòng đã rời.
            sessionStore.setCurrentRoom(null);
            sessionStore.setCurrentRoomId(null);
        }

        if (sceneNavigator != null) {
            sceneNavigator.showLobby();
        }

        if (lobbyUserInfoBinder != null && sessionStore != null) {
            lobbyUserInfoBinder.bind(sessionStore.getCurrentUser());
        }

        if (auctionService != null) {
            auctionService.getRooms();
        }
    }
}
