package com.auction.client.feature.controllers.assistant.chatbot.message;

import javafx.scene.control.Label;

public class ChatMessageBubbleFactory {
    public Label create(String message, String styleClass) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add(styleClass);
        return bubble;
    }
}
