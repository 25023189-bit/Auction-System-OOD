package com.auction.client.feature.controllers;

import com.auction.client.service.ChatbotService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ChatbotController {

    @FXML private StackPane chatbotRoot;
    @FXML private VBox chatbotPanel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField txtChatbotInput;
    @FXML private Button btnOpenChatbot;
    @FXML private Button btnCloseChatbot;
    @FXML private Button btnSendChatbot;

    private final ChatbotService chatbotService = new ChatbotService();

    @FXML
    public void initialize() {
        configureInteraction();
        setChatbotVisible(false);
        addBotMessage("Xin chào, tôi có thể hỗ trợ bạn về đấu giá, anti-sniping, đặt giá và số dư.");

        if (txtChatbotInput != null) {
            txtChatbotInput.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    sendChatbotMessage();
                    event.consume();
                }
            });
        }

        if (btnOpenChatbot != null) {
            btnOpenChatbot.setOnAction(this::showChatbot);
        }
        if (btnCloseChatbot != null) {
            btnCloseChatbot.setOnAction(this::hideChatbot);
        }
        if (btnSendChatbot != null) {
            btnSendChatbot.setOnAction(this::handleSendChatbotMessage);
        }
    }

    private void configureInteraction() {
        if (chatbotRoot != null) {
            chatbotRoot.setPickOnBounds(false);
            chatbotRoot.setMouseTransparent(false);
        }
        if (chatbotPanel != null) {
            chatbotPanel.setPickOnBounds(true);
            chatbotPanel.setDisable(false);
            chatbotPanel.setMouseTransparent(false);
        }
        if (btnOpenChatbot != null) {
            btnOpenChatbot.setFocusTraversable(false);
            btnOpenChatbot.setDisable(false);
            btnOpenChatbot.setMouseTransparent(false);
        }
        if (btnCloseChatbot != null) {
            btnCloseChatbot.setFocusTraversable(false);
            btnCloseChatbot.setDisable(false);
            btnCloseChatbot.setMouseTransparent(false);
        }
        if (btnSendChatbot != null) {
            btnSendChatbot.setDisable(false);
            btnSendChatbot.setMouseTransparent(false);
        }
        if (txtChatbotInput != null) {
            txtChatbotInput.setDisable(false);
            txtChatbotInput.setEditable(true);
            txtChatbotInput.setMouseTransparent(false);
        }
    }

    @FXML
    public void showChatbot(ActionEvent event) {
        setChatbotVisible(true);
        Platform.runLater(() -> txtChatbotInput.requestFocus());
    }

    @FXML
    public void hideChatbot(ActionEvent event) {
        setChatbotVisible(false);
    }

    @FXML
    public void handleSendChatbotMessage(ActionEvent event) {
        sendChatbotMessage();
    }

    private void setChatbotVisible(boolean visible) {
        chatbotPanel.setVisible(visible);
        chatbotPanel.setManaged(visible);
        chatbotPanel.setDisable(!visible);
        chatbotPanel.setMouseTransparent(!visible);

        btnOpenChatbot.setVisible(!visible);
        btnOpenChatbot.setManaged(!visible);
        btnOpenChatbot.setDisable(visible);
        btnOpenChatbot.setMouseTransparent(visible);

        if (visible) {
            chatbotPanel.toFront();
        } else {
            btnOpenChatbot.toFront();
        }
    }

    private void sendChatbotMessage() {
        String userMessage = txtChatbotInput != null ? txtChatbotInput.getText().trim() : "";
        if (userMessage.isEmpty()) {
            return;
        }

        addUserMessage(userMessage);
        addBotMessage(chatbotService.reply(userMessage));
        txtChatbotInput.clear();
    }

    private void addUserMessage(String message) {
        addMessage(message, "chatbot-message-user", Pos.CENTER_RIGHT);
    }

    private void addBotMessage(String message) {
        addMessage(message, "chatbot-message-bot", Pos.CENTER_LEFT);
    }

    private void addMessage(String message, String styleClass, Pos alignment) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add(styleClass);

        HBox row = new HBox(bubble);
        row.setAlignment(alignment);
        row.getStyleClass().add("chatbot-message-row");

        chatMessages.getChildren().add(row);
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }
}
