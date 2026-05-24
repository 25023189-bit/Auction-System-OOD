package com.auction.client.feature.controllers.app.root;

import com.auction.client.shared.support.DefaultUiResetService;
import com.auction.client.shared.support.UiResetService;

final class AuctionControllerViewServices {
    private AuctionControllerViewServices() {
    }

    static UiResetService createUiResetService(AuctionController controller) {
        return new DefaultUiResetService(
                controller.btnCreateAuction,
                controller.btnCloseAuction,
                controller.btnPlaceBid,
                controller.txtBidAmount,
                controller.txtChatInput,
                controller.txtChatLog,
                controller.lblUsername,
                controller.lblBalance,
                controller.lblAuctionItemName,
                controller.lblCurrentPrice,
                controller.lblTimer
        );
    }
}
