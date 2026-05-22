package com.auction.client.feature.controllers.assistant.chatbot.message;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatMessageRenderer {
    private final VBox chatMessages;
    private final ChatMessageBubbleFactory bubbleFactory;

    public ChatMessageRenderer(VBox chatMessages, ChatMessageBubbleFactory bubbleFactory) {
        this.chatMessages = chatMessages;
        this.bubbleFactory = bubbleFactory;
    }

    public Label render(String message, String styleClass, Pos alignment) {
        Label bubble = bubbleFactory.create(message, styleClass);
        HBox messageRow = new HBox(bubble);
        messageRow.setAlignment(alignment);
        if (chatMessages != null) {
            chatMessages.getChildren().add(messageRow);
        }
        return bubble;
    }
}
