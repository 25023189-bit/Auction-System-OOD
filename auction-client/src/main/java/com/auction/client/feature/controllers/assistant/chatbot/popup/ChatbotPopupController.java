package com.auction.client.feature.controllers.assistant.chatbot.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.net.URL;

public class ChatbotPopupController {
    private Popup activePopup;

    public void showChatbot(ActionEvent event) {
        Node source = event != null && event.getSource() instanceof Node node ? node : null;
        if (source == null || source.getScene() == null) {
            return;
        }

        try {
            activePopup = show(source.getScene().getWindow(), activePopup);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to open chatbot view.", ex);
        }
    }

    public void hideChatbot(ActionEvent event) {
        if (activePopup != null) {
            activePopup.hide();
            activePopup = null;
            return;
        }

        Node source = event != null && event.getSource() instanceof Node node ? node : null;
        if (source != null && source.getScene() != null && source.getScene().getWindow() != null) {
            source.getScene().getWindow().hide();
        }
    }

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
