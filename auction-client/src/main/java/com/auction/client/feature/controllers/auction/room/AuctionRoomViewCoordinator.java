package com.auction.client.feature.controllers.auction.room;

import com.auction.client.core.navigation.SceneNavigator;
import com.auction.client.feature.controllers.auction.autobid.AutoBidController;
import com.auction.client.feature.controllers.auction.room.bidding.BidController;
import com.auction.client.feature.controllers.auction.room.chat.RoomChatController;
import com.auction.client.feature.controllers.auction.room.lifecycle.CloseAuctionController;
import com.auction.client.feature.controllers.auction.room.visual.AuctionRoomUserController;
import com.auction.client.feature.controllers.auction.room.visual.PriceChartController;
import com.auction.client.feature.lobby.LobbyUserInfoBinder;
import com.auction.client.feature.room.AuctionCloseHandler;
import com.auction.client.feature.room.AuctionRoomMessageHandler;
import com.auction.client.feature.room.AuctionRoomPresenter;
import com.auction.client.feature.room.AuctionRoomStateBinder;
import com.auction.client.feature.room.AuctionTimer;
import com.auction.client.feature.room.BidActionHandler;
import com.auction.client.feature.room.ChatActionHandler;
import com.auction.client.feature.room.DefaultAuctionTimerService;
import com.auction.client.feature.room.RoomTransitionHandler;
import com.auction.client.network.messaging.MessageHandler;
import com.auction.client.network.socket.ClientConnection;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.utils.ImageUtils;
import com.auction.common.model.AuctionRoom;
import com.auction.common.role.RolePolicy;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class AuctionRoomViewCoordinator {
    private final AuctionRoomPresenter auctionRoomPresenter;
    private final AuctionTimer auctionTimerService;
    private final AuctionRoomStateBinder auctionRoomStateBinder;
    private final MessageHandler auctionRoomMessageHandler;
    private final RoomTransitionHandler roomTransitionHandler;
    private final BidController bidController;
    private final RoomChatController roomChatController;
    private final CloseAuctionController closeAuctionController;
    private final AutoBidController autoBidController;
    private final AuctionRoomUserController auctionRoomUserController;
    private PriceChartController priceChartController;
    private final ImageView imgProduct;

    public AuctionRoomViewCoordinator(
            Label lblAuctionItemName,
            Label lblProductName,
            Label lblCurrentPrice,
            Label lblTimer,
            Label lblTimeLeft,
            Label lblParticipantCount,
            Label lblDescription,
            TextArea txtChatLog,
            TextArea txtItemDescriptionDisplay,
            Button btnCloseAuction,
            Button btnPlaceBid,
            TextField txtBidAmount,
            TextField txtChatInput,
            Label lblUsername,
            ImageView imgProduct,
            AreaChart<String, Number> priceChart,
            Button btnToggleAutoBid,
            VBox paneAutoBid,
            TextField txtMaxBid,
            TextField txtAutoBidStep,
            AuctionService auctionService,
            SessionStore sessionStore,
            RolePolicy rolePolicy,
            SceneNavigator sceneNavigator,
            ClientConnection clientConnection,
            LobbyUserInfoBinder lobbyUserInfoBinder
    ) {
        // Lưu đối tượng ImageView từ tham số truyền vào
        this.imgProduct = imgProduct;

        Label itemNameLabel = lblAuctionItemName != null ? lblAuctionItemName : lblProductName;
        Label timerLabel = lblTimer != null ? lblTimer : lblTimeLeft;
        auctionRoomPresenter = new AuctionRoomPresenter(
                itemNameLabel, lblCurrentPrice, timerLabel, lblParticipantCount,
                lblDescription, txtChatLog, txtItemDescriptionDisplay,
                btnCloseAuction, btnPlaceBid, txtBidAmount
        );
        auctionTimerService = new DefaultAuctionTimerService(auctionRoomPresenter);
        auctionRoomStateBinder = new AuctionRoomStateBinder(auctionRoomPresenter, sessionStore, rolePolicy);
        autoBidController = new AutoBidController(
                paneAutoBid,
                txtMaxBid,
                txtAutoBidStep,
                btnToggleAutoBid,
                txtChatLog,
                sessionStore,
                clientConnection
        );
        auctionRoomMessageHandler = new AuctionRoomMessageHandler(
                sessionStore, sceneNavigator, auctionRoomStateBinder, auctionRoomPresenter, auctionTimerService, autoBidController
        );
        BidActionHandler bidActionHandler = new BidActionHandler(auctionService, sessionStore, auctionRoomPresenter);
        ChatActionHandler chatActionHandler = new ChatActionHandler(auctionService);
        AuctionCloseHandler auctionCloseHandler = new AuctionCloseHandler(auctionService);
        bidController = new BidController(bidActionHandler, txtBidAmount);
        roomChatController = new RoomChatController(chatActionHandler, txtChatInput);
        closeAuctionController = new CloseAuctionController(auctionCloseHandler, sessionStore, btnCloseAuction);
        roomTransitionHandler = new RoomTransitionHandler(
                auctionService, sessionStore, sceneNavigator, auctionTimerService, lobbyUserInfoBinder
        );
        auctionRoomUserController = new AuctionRoomUserController(lblUsername);

        if (btnCloseAuction != null) {
            btnCloseAuction.setVisible(false);
            btnCloseAuction.setManaged(false);
        }
        bindCurrentUser(sessionStore);
        bindCurrentRoom(sessionStore, priceChart, lblCurrentPrice);
    }

    public void handleBid() {
        bidController.handleBid();
    }

    public void handleSendChat() {
        roomChatController.handleSendChat();
    }

    public void handleCloseAuction() {
        closeAuctionController.handleCloseAuction();
    }

    public void handleBackToSelection() {
        roomTransitionHandler.backToLobby();
    }

    public void toggleAutoBidPanel() {
        autoBidController.togglePanel();
    }

    public void handleSetAutoBid() {
        autoBidController.handleSetAutoBid();
    }

    public void handleCancelAutoBid() {
        autoBidController.handleCancelAutoBid();
    }

    public MessageHandler auctionRoomMessageHandler() {
        return auctionRoomMessageHandler;
    }

    public AuctionRoomPresenter auctionRoomPresenter() {
        return auctionRoomPresenter;
    }

    public AuctionTimer auctionTimerService() {
        return auctionTimerService;
    }

    private void bindCurrentUser(SessionStore sessionStore) {
        if (sessionStore != null) {
            auctionRoomUserController.bind(sessionStore.getCurrentUser());
        }
    }

    private void bindCurrentRoom(SessionStore sessionStore, AreaChart<String, Number> priceChart, Label lblCurrentPrice) {
        if (sessionStore == null || sessionStore.getCurrentRoom() == null) {
            return;
        }

        AuctionRoom room = sessionStore.getCurrentRoom();

        // 🌟 ĐÃ THÊM: Xử lý giải mã và hiển thị hình ảnh sản phẩm
        ImageUtils.applyBase64OrPlaceholder(imgProduct, room.getBase64Image());

        auctionRoomStateBinder.bind(room);
        auctionTimerService.start(
                room.getStartTime(),
                room.getScheduledEndTime() != null ? room.getScheduledEndTime() : room.getEndTime()
        );
        closeAuctionController.updateButtonVisibility();

        if (priceChartController == null) {
            priceChartController = new PriceChartController(priceChart, lblCurrentPrice);
        }
        priceChartController.initializeWithStartPrice(room.getStartingPrice());
        priceChartController.bindCurrentPriceLabel();
    }
}
