package com.auction.client.feature.controllers.assistant.chatbot.popup;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.net.URL;

public class ChatbotPopupController {
    public Popup show(Window owner, Popup currentPopup) throws Exception {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.hide();
        }

        URL fxml = getClass().getResource("/com/example/auctionprototype/fxml/chatbot-chatting-view.fxml");
        if (fxml == null) {
            throw new IllegalStateException("Missing chatbot-chatting-view.fxml.");
        }

        Parent content = new FXMLLoader(fxml).load();
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.getContent().setAll(content);
        popup.show(owner, owner.getX() + owner.getWidth() - 380, owner.getY() + owner.getHeight() - 540);
        return popup;
    }
}
