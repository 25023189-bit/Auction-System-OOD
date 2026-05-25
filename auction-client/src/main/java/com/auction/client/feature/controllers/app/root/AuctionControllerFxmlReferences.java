package com.auction.client.feature.controllers.app.root;

final class AuctionControllerFxmlReferences {
    private AuctionControllerFxmlReferences() {
    }

    static void clear(AuctionController controller) {
        // Auth view nodes
        controller.paneLogin = null;
        controller.paneRegister = null;
        controller.paneForgotPassword = null;

        // Main screen roots
        controller.paneAuctionRoom = null;
        controller.paneMainLobby = null;

        // Auth fields
        controller.txtUsername = null;
        controller.txtPassword = null;
        controller.txtRegCustomerId = null;
        controller.txtRegUsername = null;
        controller.txtRegFullName = null;
        controller.txtRegPassword = null;
        controller.txtRegConfirm = null;
        controller.txtRegOrganization = null;
        controller.txtForgotUsername = null;
        controller.txtForgotNewPassword = null;
        controller.txtForgotConfirm = null;
        controller.cbRegRole = null;
        controller.lblStatus = null;
        controller.lblRegStatus = null;
        controller.lblForgotStatus = null;
        controller.lblRegOrganization = null;

        // Lobby fields
        controller.lblUsername = null;
        controller.lblBalance = null;
        controller.lblUsernameDisplay = null;
        controller.lblCountdownTimer = null;
        controller.btnCreateAuction = null;
        controller.paneSelectAuction = null;

        // Auction room fields
        controller.lblAuctionItemName = null;
        controller.lblProductName = null;
        controller.lblCurrentPrice = null;
        controller.lblParticipantCount = null;
        controller.lblTimer = null;
        controller.lblTimeLeft = null;
        controller.lblDescription = null;
        controller.txtChatLog = null;
        controller.txtItemDescriptionDisplay = null;
        controller.txtBidAmount = null;
        controller.txtChatInput = null;
        controller.btnCloseAuction = null;
        controller.btnPlaceBid = null;
        controller.imgProduct = null;
        controller.priceChart = null;

        // Autobid fields
        controller.btnToggleAutoBid = null;
        controller.paneAutoBid = null;
        controller.txtMaxBid = null;
        controller.txtAutoBidStep = null;

        // Optional dashboard/news fields
        controller.vboxCurrencyRates = null;
        controller.vboxNews = null;
    }
}
