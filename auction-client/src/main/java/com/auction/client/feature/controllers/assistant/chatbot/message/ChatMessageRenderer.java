package com.auction.client.feature.controllers.assistant.chatbot.message;

import javafx.application.Platform;
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

    public Label addUserMessage(String message) {
        return addMessage(message, "chatbot-message-user", Pos.CENTER_RIGHT);
    }

    public Label addBotMessage(String message) {
        return addMessage(message, "chatbot-message-bot", Pos.CENTER_LEFT);
    }

    public Label addMessage(String message, String styleClass, Pos alignment) {
        if (Platform.isFxApplicationThread()) {
            return render(message, styleClass, alignment);
        }

        final Label[] bubble = new Label[1];
        Platform.runLater(() -> bubble[0] = render(message, styleClass, alignment));
        return bubble[0];
    }

    public void updateMessage(Label bubble, String message) {
        if (bubble == null) {
            addBotMessage(message);
            return;
        }
        if (Platform.isFxApplicationThread()) {
            bubble.setText(message);
        } else {
            Platform.runLater(() -> bubble.setText(message));
        }
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
