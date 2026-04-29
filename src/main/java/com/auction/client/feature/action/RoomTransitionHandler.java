package com.auction.client.feature.action;

import com.auction.server.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.presenter.AuctionTimerService;
import com.auction.client.feature.presenter.LobbyUserInfoBinder;

/**
 * Handler chuyển phòng phiên bản dùng cho package action cũ.
 */
public class RoomTransitionHandler {
    private AuctionService auctionService;
    private SessionStore sessionStore;
    private SceneNavigator sceneNavigator;
    private AuctionTimerService timerService;
    private LobbyUserInfoBinder userInfoBinder;

    public RoomTransitionHandler(AuctionService auctionService, SessionStore sessionStore,
                                 SceneNavigator sceneNavigator, AuctionTimerService timerService,
                                 LobbyUserInfoBinder userInfoBinder) {
        this.auctionService = auctionService;
        this.sessionStore = sessionStore;
        this.sceneNavigator = sceneNavigator;
        this.timerService = timerService;
        this.userInfoBinder = userInfoBinder;
    }

    public void joinRoom(String roomId) {
        if (auctionService != null) {
            // Gửi yêu cầu tham gia phòng lên server; server trả ROOM_JOINED nếu thành công.
            auctionService.joinRoom(roomId);
        }
    }

    public void leaveRoom(String roomId) {
        if (auctionService != null) {
            // Service rời phòng hiện tại nên không cần truyền roomId xuống nữa.
            auctionService.leaveRoom();
        }
        if (timerService != null) {
            timerService.stop();
        }
    }

    // Điểm gọi tiện lợi khi UI muốn rời phòng hiện tại và quay về lobby.
    public void backToLobby() {
        if (sessionStore != null) {
            String currentRoomId = sessionStore.getCurrentRoomId();
            if (currentRoomId != null) {
                leaveRoom(currentRoomId);
            }
        }
    }
}
