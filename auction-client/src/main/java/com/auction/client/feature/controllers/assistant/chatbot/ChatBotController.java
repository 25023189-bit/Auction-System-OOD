package com.auction.client.feature.controllers.assistant.chatbot;

import com.auction.client.AI.chatbot.ChatbotFallback;
import com.auction.client.feature.controllers.assistant.chatbot.message.ChatMessageBubbleFactory;
import com.auction.client.feature.controllers.assistant.chatbot.message.ChatMessageRenderer;
import com.auction.client.feature.controllers.assistant.chatbot.request.ChatbotRequestRunner;
import com.auction.client.feature.controllers.assistant.chatbot.window.ChatbotWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class ChatBotController {

    private static final String WAITING_MESSAGE = "Dang xu ly cau hoi...";

    @FXML private VBox chatbotPanel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField txtChatbotInput;
    @FXML private Button btnOpenChatbot;
    @FXML private Button btnCloseChatbot;
    @FXML private Button btnSendChatbot;

    private ChatbotWindowController windowController;
    private ChatbotRequestRunner requestRunner;
    private ChatMessageRenderer messageRenderer;

    @FXML
    public void initialize() {
        windowController = new ChatbotWindowController();
        requestRunner = new ChatbotRequestRunner();
        messageRenderer = new ChatMessageRenderer(chatMessages, new ChatMessageBubbleFactory());

        if (btnOpenChatbot != null) {
            btnOpenChatbot.setFocusTraversable(false);
            btnOpenChatbot.setOnAction(this::showChatbot);
        }
        if (btnCloseChatbot != null) {
            btnCloseChatbot.setOnAction(this::hideChatbot);
        }
        if (btnSendChatbot != null) {
            btnSendChatbot.setOnAction(this::handleSendChatbotMessage);
        }
        if (txtChatbotInput != null) {
            txtChatbotInput.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    sendChatbotMessage();
                    event.consume();
                }
            });
        }
        if (chatScrollPane != null && chatMessages != null) {
            chatMessages.heightProperty().addListener((observable, oldValue, newValue) -> chatScrollPane.setVvalue(1.0));
        }
        if (chatMessages != null && chatMessages.getChildren().isEmpty()) {
            messageRenderer.addBotMessage("Xin chao, toi co the giai thich va huong dan ban su dung he thong dau gia.");
        }
    }

    @FXML
    public void showChatbot(ActionEvent event) {
        windowController.showChatbot(event);
    }

    @FXML
    public void hideChatbot(ActionEvent event) {
        windowController.hideChatbot(event);
    }

    @FXML
    public void handleSendChatbotMessage(ActionEvent event) {
        sendChatbotMessage();
    }

    @FXML
    public void handleSendChatbotMessage() {
        sendChatbotMessage();
    }

    @FXML
    public void sendChatbotMessage(ActionEvent event) {
        sendChatbotMessage();
    }

    private void sendChatbotMessage() {
        String userMessage = txtChatbotInput != null ? txtChatbotInput.getText().trim() : "";
        if (userMessage.isEmpty()) {
            return;
        }

        messageRenderer.addUserMessage(userMessage);
        var waitingBubble = messageRenderer.addBotMessage(WAITING_MESSAGE);
        txtChatbotInput.clear();
        setSending(true);

        requestRunner.askAsync(
                userMessage,
                botResponse -> {
                    setSending(false);
                    messageRenderer.updateMessage(waitingBubble, normalizeBotResponse(botResponse));
                },
                throwable -> {
                    setSending(false);
                    messageRenderer.updateMessage(waitingBubble, ChatbotFallback.MESSAGE);
                }
        );
    }

    private String normalizeBotResponse(String botResponse) {
        return botResponse == null || botResponse.trim().isEmpty() ? ChatbotFallback.MESSAGE : botResponse;
    }

    private void setSending(boolean sending) {
        if (txtChatbotInput != null) {
            txtChatbotInput.setDisable(sending);
        }
        if (btnSendChatbot != null) {
            btnSendChatbot.setDisable(sending);
        }
    }
}
