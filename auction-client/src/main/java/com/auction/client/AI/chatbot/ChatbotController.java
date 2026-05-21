package com.auction.client.AI.chatbot;

import javafx.application.Platform;
import javafx.concurrent.Task;
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

/**
 * Controller điều khiển khung chatbot nổi trên giao diện JavaFX.
 * Vai trò:
 * - Quản lý trạng thái mở/đóng panel chatbot và render bubble tin nhắn.
 * - Gửi câu hỏi sang chatbot Python bằng task nền để không khóa JavaFX Application Thread.
 * Luồng chính:
 * 1. initialize() cấu hình interaction, sự kiện nút/Enter và tin nhắn chào ban đầu.
 * 2. Người dùng gửi câu hỏi, controller hiển thị tin user, gọi PythonChatbotConnection và render phản hồi bot.
 * Business rules:
 * - Không gửi câu hỏi rỗng và khóa input tạm thời khi đang chờ phản hồi.
 * - Khi Python lỗi hoặc phản hồi không hợp lệ thì hiển thị ChatbotFallback.MESSAGE.
 * Ghi chú kỹ thuật:
 * - Không thread-safe: control JavaFX chỉ được cập nhật trên JavaFX Application Thread.
 * Dependency: FXML controls, Task, Platform, PythonChatbotConnection, ChatbotFallback.
 */
public class ChatbotController {

    @FXML private StackPane chatbotRoot;
    @FXML private VBox chatbotPanel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField txtChatbotInput;
    @FXML private Button btnOpenChatbot;
    @FXML private Button btnCloseChatbot;
    @FXML private Button btnSendChatbot;

    // Sử dụng cơ chế Singleton mẫu để lấy kết nối duy nhất đến kịch bản Python
    private final PythonChatbotConnection chatbotConnection = PythonChatbotConnection.getInstance();

    @FXML
    public void initialize() {
        // Cấu hình tương tác giao diện và thiết lập ẩn panel ban đầu
        configureInteraction();
        setChatbotVisible(false);
        addBotMessage("Xin chào, tôi có thể giải thích và hướng dẫn bạn sử dụng hệ thống đấu giá.");

        // Bổ sung sự kiện lắng nghe phím Enter trực tiếp trên ô nhập TextField
        if (txtChatbotInput != null) {
            txtChatbotInput.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    sendChatbotMessage();
                }
            });
        }

        // TỐI ƯU HÓA: Đăng ký Auto-scroll DUY NHẤT một lần tại đây khi khởi tạo
        // Tránh lỗi tạo hàng loạt listener gây Memory Leak (tràn bộ nhớ) khi thêm nhiều tin nhắn.
        if (chatScrollPane != null && chatMessages != null) {
            chatMessages.heightProperty().addListener((observable, oldValue, newValue) ->
                    chatScrollPane.setVvalue(1.0)
            );
        }
    }

    // === CÁC HÀM HOOK SỰ KIỆN ĐỂ KHỚP CHÍNH XÁC VỚI FILE FXML (DỰ PHÒNG CÁC TRƯỜNG HỢP) ===

    @FXML
    public void handleSendChatbotMessage(ActionEvent event) {
        sendChatbotMessage();
    }

    /**
     * Bọc lót trường hợp file FXML gọi hàm handleSendChatbotMessage không truyền tham số ActionEvent
     */
    @FXML
    public void handleSendChatbotMessage() {
        sendChatbotMessage();
    }

    /**
     * Bọc lót trường hợp nút bấm trong file FXML đặt thuộc tính onAction="#sendChatbotMessage"
     */
    @FXML
    public void sendChatbotMessage(ActionEvent event) {
        sendChatbotMessage();
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

    // === CÁC HÀM LÝ THUYẾT XỬ LÝ LOGIC NGHIỆP VỤ NỀN ===

    /**
     * Cấu hình thuộc tính chuột và tương tác cho các Node lồng nhau
     * Nhằm tránh việc Popup Chatbot ẩn chiếm dụng / chặn sự kiện bấm chuột của màn hình bên dưới.
     */
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

    private void setChatbotVisible(boolean visible) {
        if (chatbotPanel != null) {
            chatbotPanel.setVisible(visible);
            chatbotPanel.setManaged(visible);
            chatbotPanel.setDisable(!visible);
            chatbotPanel.setMouseTransparent(!visible);
        }
    }

    /**
     * Xử lý chính việc đọc chuỗi ký tự, hiển thị nội dung phía User,
     * thiết lập Task nền song song để trao đổi dữ liệu với Python qua File System.
     */
    private void sendChatbotMessage() {
        String userMessage = txtChatbotInput != null ? txtChatbotInput.getText().trim() : "";
        if (userMessage.isEmpty()) {
            return;
        }

        // 1. Kết xuất dòng text của người dùng lên màn hình ngay lập tức
        addUserMessage(userMessage);
        txtChatbotInput.clear();

        // 2. Chuyển trạng thái nhập liệu thành "Bận" để phòng ngừa hành vi nhấn gửi liên tục gây dồn ứ Task
        setSending(true);

        // 3. Khởi tạo một Task nghiệp vụ bất đồng bộ kế thừa mô hình JavaFX Concurrency
        Task<String> chatbotTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // Tác vụ I/O nặng này sẽ được điều hướng thực thi ở Worker Thread độc lập
                return chatbotConnection.sendQuery(userMessage);
            }
        };

        // 4. Định nghĩa hàm gọi khi tác vụ xử lý tệp tin ngầm phản hồi thành công
        chatbotTask.setOnSucceeded(event -> {
            setSending(false);
            String botResponse = chatbotTask.getValue();
            if (botResponse == null || botResponse.trim().isEmpty()) {
                addBotMessage(ChatbotFallback.MESSAGE);
            } else {
                addBotMessage(botResponse);
            }
        });

        // 5. Khôi phục trạng thái điều khiển nếu xảy ra lỗi phần cứng hoặc ngoại lệ ngoài ý muốn
        chatbotTask.setOnFailed(event -> {
            setSending(false);
            addBotMessage(ChatbotFallback.MESSAGE);
        });

        // 6. Gói Task vào một Daemon Thread để hệ thống thu hồi tài nguyên sạch sẽ khi thoát ứng dụng
        Thread backgroundThread = new Thread(chatbotTask);
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

    private void addBotMessage(String message) {
        addMessage(message, "chatbot-message-bot", Pos.CENTER_LEFT);
    }

    /**
     * Dựng thành phần đồ họa (Node Component) đại diện cho bong bóng tin nhắn.
     */
    private void addMessage(String message, String styleClass, Pos alignment) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add(styleClass);

        HBox messageRow = new HBox(bubble);
        messageRow.setAlignment(alignment);

        if (chatMessages != null) {
            chatMessages.getChildren().add(messageRow);
        }
    }
}