package com.auction.client.feature.controllers.assistant.chatbot.window;

import com.auction.client.feature.controllers.assistant.chatbot.popup.ChatbotPopupController;
import javafx.event.ActionEvent;

public class ChatbotWindowController {
    private final ChatbotPopupController popupController = new ChatbotPopupController();

    public void showChatbot(ActionEvent event) {
        popupController.showChatbot(event);
    }

    public void hideChatbot(ActionEvent event) {
        popupController.hideChatbot(event);
    }
}
