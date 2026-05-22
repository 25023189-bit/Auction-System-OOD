package com.auction.client.feature.controllers.assistant.chatbot;

import com.auction.client.AI.chatbot.ChatbotFallback;
import com.auction.client.AI.chatbot.PythonChatbotConnection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class ChatBotController {

    private static Popup activePopup;
    private static final String WAITING_MESSAGE = "Đang xử lý câu hỏi...";

    @FXML private VBox chatbotPanel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField txtChatbotInput;
    @FXML private Button btnOpenChatbot;
    @FXML private Button btnCloseChatbot;
    @FXML private Button btnSendChatbot;

    private final PythonChatbotConnection chatbotConnection;

    public ChatBotController() {
        this(PythonChatbotConnection.getInstance());
    }

    ChatBotController(PythonChatbotConnection chatbotConnection) {
        this.chatbotConnection = chatbotConnection;
    }

    @FXML
    public void initialize() {
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
            chatMessages.heightProperty().addListener((observable, oldValue, newValue) ->
                    chatScrollPane.setVvalue(1.0)
            );
        }
        if (chatMessages != null && chatMessages.getChildren().isEmpty()) {
            addBotMessage("Xin chào, tôi có thể giải thích và hướng dẫn bạn sử dụng hệ thống đấu giá.");
        }
    }

    @FXML
    public void showChatbot(ActionEvent event) {
        Node source = event != null && event.getSource() instanceof Node node ? node : btnOpenChatbot;
        if (source == null || source.getScene() == null) {
            return;
        }

        Window owner = source.getScene().getWindow();
        if (activePopup != null && activePopup.isShowing()) {
            activePopup.hide();
        }

        try {
            URL fxml = getClass().getResource("/com/example/auctionprototype/fxml/chatbot-chatting-view.fxml");
            if (fxml == null) {
                throw new IllegalStateException("Missing chatbot-chatting-view.fxml.");
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent content = loader.load();
            activePopup = new Popup();
            activePopup.setAutoHide(false);
            activePopup.getContent().setAll(content);
            activePopup.show(owner, owner.getX() + owner.getWidth() - 380, owner.getY() + owner.getHeight() - 540);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to open chatbot view.", ex);
        }
    }

    @FXML
    public void hideChatbot(ActionEvent event) {
        if (activePopup != null) {
            activePopup.hide();
            activePopup = null;
            return;
        }
        Node source = event != null && event.getSource() instanceof Node node ? node : btnCloseChatbot;
        if (source != null && source.getScene() != null && source.getScene().getWindow() != null) {
            source.getScene().getWindow().hide();
        }
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

        addUserMessage(userMessage);
        Label waitingBubble = addBotMessage(WAITING_MESSAGE);
        txtChatbotInput.clear();
        setSending(true);

        Task<String> chatbotTask = new Task<>() {
            @Override
            protected String call() {
                return chatbotConnection.ask(userMessage);
            }
        };

        chatbotTask.setOnSucceeded(event -> {
            setSending(false);
            String botResponse = chatbotTask.getValue();
            updateBotMessage(waitingBubble, botResponse == null || botResponse.trim().isEmpty()
                    ? ChatbotFallback.MESSAGE
                    : botResponse);
        });

        chatbotTask.setOnFailed(event -> {
            setSending(false);
            updateBotMessage(waitingBubble, ChatbotFallback.MESSAGE);
        });

        Thread backgroundThread = new Thread(chatbotTask, "auction-chatbot-request");
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void setSending(boolean sending) {
        if (txtChatbotInput != null) {
            txtChatbotInput.setDisable(sending);
        }
        if (btnSendChatbot != null) {
            btnSendChatbot.setDisable(sending);
        }
    }

    private void addUserMessage(String message) {
        addMessage(message, "chatbot-message-user", Pos.CENTER_RIGHT);
    }

    private Label addBotMessage(String message) {
        return addMessage(message, "chatbot-message-bot", Pos.CENTER_LEFT);
    }

    private Label addMessage(String message, String styleClass, Pos alignment) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add(styleClass);

        HBox messageRow = new HBox(bubble);
        messageRow.setAlignment(alignment);

        if (chatMessages != null) {
            if (Platform.isFxApplicationThread()) {
                chatMessages.getChildren().add(messageRow);
            } else {
                Platform.runLater(() -> chatMessages.getChildren().add(messageRow));
            }
        }
        return bubble;
    }

    private void updateBotMessage(Label bubble, String message) {
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
}
