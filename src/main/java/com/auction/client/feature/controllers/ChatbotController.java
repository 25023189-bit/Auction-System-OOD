package com.auction.client.feature.controllers;

import com.auction.client.service.ChatbotService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private Label balanceLabel;
    private FlowPane auctionListPane;

    @FXML
    public void initialize() {
        configureInteraction();
        setChatbotVisible(false);
        addBotMessage("Xin chao, toi co the ho tro ve dau gia, anti-sniping, dat gia, so du va truy van san pham dang dau gia.");

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

    public void setLobbyContext(Label balanceLabel, FlowPane auctionListPane) {
        this.balanceLabel = balanceLabel;
        this.auctionListPane = auctionListPane;
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
        addBotMessage(resolveChatbotReply(userMessage));
        txtChatbotInput.clear();
    }

    private String resolveChatbotReply(String userMessage) {
        String normalizedMessage = normalizeChatbotText(userMessage);

        if (isBalanceQuery(normalizedMessage)) {
            return getBalanceQueryReply();
        }
        if (isAuctionCountQuery(normalizedMessage)) {
            return getAuctionCountQueryReply();
        }
        if (isProductPriceQuery(normalizedMessage)) {
            String productName = extractProductSearchTerm(normalizedMessage);
            if (!productName.isBlank()) {
                return getProductPriceQueryReply(productName);
            }
        }
        if (isProductExistenceQuery(normalizedMessage)) {
            String productName = extractProductSearchTerm(normalizedMessage);
            return getProductExistenceQueryReply(productName);
        }

        return chatbotService.reply(userMessage);
    }

    private boolean isBalanceQuery(String message) {
        return containsAny(message, "so du", "balance", "tai khoan con bao nhieu", "tien trong tai khoan");
    }

    private boolean isAuctionCountQuery(String message) {
        return containsAny(message, "so phien", "bao nhieu phien", "phien dang dien ra", "phien dang mo", "co may phien");
    }

    private boolean isProductExistenceQuery(String message) {
        return containsAny(message, "co san pham", "ton tai san pham", "san pham dang dau gia", "kiem tra san pham", "tim san pham");
    }

    private boolean isProductPriceQuery(String message) {
        return message.contains("gia") && !message.contains("dat gia") && !message.contains("gia han");
    }

    private String getBalanceQueryReply() {
        if (balanceLabel == null || balanceLabel.getText() == null || balanceLabel.getText().isBlank()) {
            return "Toi chua doc duoc so du tai khoan tren man hinh hien tai.";
        }

        return "So du tai khoan hien tai cua ban la: " + balanceLabel.getText().replace("Balance:", "").trim();
    }

    private String getAuctionCountQueryReply() {
        List<ProductSnapshot> products = getDisplayedAuctionProducts();
        if (products.isEmpty()) {
            return "Hien tai lobby khong co phien dau gia nao dang hien thi.";
        }

        return "Hien tai co " + products.size() + " phien dau gia dang hien thi trong lobby.";
    }

    private String getProductExistenceQueryReply(String productName) {
        List<ProductSnapshot> products = getDisplayedAuctionProducts();
        if (products.isEmpty()) {
            return "Hien tai khong co san pham nao dang dau gia trong lobby.";
        }

        if (productName.isBlank()) {
            return "Hien tai co " + products.size() + " san pham dang dau gia: " + joinProductNames(products) + ".";
        }

        return products.stream()
                .filter(product -> normalizeChatbotText(product.name()).contains(productName))
                .findFirst()
                .map(product -> "Co, san pham '" + product.name() + "' dang dau gia trong lobby.")
                .orElse("Khong tim thay san pham phu hop voi '" + productName + "' trong lobby hien tai.");
    }

    private String getProductPriceQueryReply(String productName) {
        List<ProductSnapshot> products = getDisplayedAuctionProducts();
        if (products.isEmpty()) {
            return "Hien tai khong co phien dau gia nao de truy van gia.";
        }

        return products.stream()
                .filter(product -> normalizeChatbotText(product.name()).contains(productName))
                .findFirst()
                .map(product -> "Gia hien tai cua '" + product.name() + "' la: " + product.price() + ".")
                .orElse("Khong tim thay san pham phu hop voi '" + productName + "' de truy van gia.");
    }

    private List<ProductSnapshot> getDisplayedAuctionProducts() {
        List<ProductSnapshot> products = new ArrayList<>();
        if (auctionListPane == null) {
            return products;
        }

        for (Node card : auctionListPane.getChildren()) {
            List<Label> labels = new ArrayList<>();
            collectLabels(card, labels);
            if (labels.isEmpty()) {
                continue;
            }

            String name = labels.get(0).getText();
            String price = labels.stream()
                    .map(Label::getText)
                    .filter(text -> text != null && normalizeChatbotText(text).startsWith("price"))
                    .findFirst()
                    .map(text -> text.replace("Price:", "").trim())
                    .orElse("chua co thong tin gia");

            if (name != null && !name.isBlank()) {
                products.add(new ProductSnapshot(name, price));
            }
        }

        return products;
    }

    private void collectLabels(Node node, List<Label> labels) {
        if (node instanceof Label label) {
            labels.add(label);
        }

        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                collectLabels(child, labels);
            }
        }
    }

    private String extractProductSearchTerm(String normalizedMessage) {
        return normalizedMessage
                .replaceAll("\\btruy van\\b", " ")
                .replaceAll("\\btra cuu\\b", " ")
                .replaceAll("\\bkiem tra\\b", " ")
                .replaceAll("\\btim kiem\\b", " ")
                .replaceAll("\\btim\\b", " ")
                .replaceAll("\\bco\\b", " ")
                .replaceAll("\\bton tai\\b", " ")
                .replaceAll("\\bsan pham\\b", " ")
                .replaceAll("\\bdang dau gia\\b", " ")
                .replaceAll("\\bdau gia\\b", " ")
                .replaceAll("\\bgia hien tai\\b", " ")
                .replaceAll("\\bgia cua\\b", " ")
                .replaceAll("\\bgia\\b", " ")
                .replaceAll("\\bbao nhieu\\b", " ")
                .replaceAll("\\bkhong\\b", " ")
                .replaceAll("\\bhay\\b", " ")
                .replaceAll("\\bgiup toi\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String joinProductNames(List<ProductSnapshot> products) {
        return products.stream()
                .map(ProductSnapshot::name)
                .limit(5)
                .reduce((left, right) -> left + ", " + right)
                .orElse("khong co san pham");
    }

    private String normalizeChatbotText(String value) {
        if (value == null) {
            return "";
        }

        String withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return withoutDiacritics
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
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

    private record ProductSnapshot(String name, String price) {
    }
}
