package com.auction.client.AI.chatbot;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatbotControllerTest {

    private ChatbotController controller;

    // Khai báo các Node đồ họa JavaFX thật để đưa vào Controller
    private StackPane chatbotRoot;
    private VBox chatbotPanel;
    private VBox chatMessages;
    private ScrollPane chatScrollPane;
    private TextField txtChatbotInput;
    private Button btnOpenChatbot;
    private Button btnCloseChatbot;
    private Button btnSendChatbot;

    @BeforeAll
    static void initJavaFXToolkit() {
        try {
            // Ép khởi chạy nền tảng JavaFX Thread để khởi tạo các Node giao diện không bị crash
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Nếu nền tảng đã chạy từ trước rồi thì bỏ qua
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new ChatbotController();

        // Khởi tạo thực thể cho các linh kiện giao diện
        chatbotRoot = new StackPane();
        chatbotPanel = new VBox();
        chatMessages = new VBox();
        chatScrollPane = new ScrollPane();
        txtChatbotInput = new TextField();
        btnOpenChatbot = new Button();
        btnCloseChatbot = new Button();
        btnSendChatbot = new Button();

        chatScrollPane.setContent(chatMessages);

        // Bơm các linh kiện này vào các trường private @FXML của Controller bằng Reflection
        injectField("chatbotRoot", chatbotRoot);
        injectField("chatbotPanel", chatbotPanel);
        injectField("chatMessages", chatMessages);
        injectField("chatScrollPane", chatScrollPane);
        injectField("txtChatbotInput", txtChatbotInput);
        injectField("btnOpenChatbot", btnOpenChatbot);
        injectField("btnCloseChatbot", btnCloseChatbot);
        injectField("btnSendChatbot", btnSendChatbot);
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = ChatbotController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    @DisplayName("Test hàm initialize cài đặt các sự kiện nút bấm và tin nhắn chào")
    void testInitialize() {
        controller.initialize();

        // Kiểm tra xem tin nhắn chào mặc định của bot đã được add vào khung chat chưa
        assertFalse(chatMessages.getChildren().isEmpty(), "Phải hiển thị lời chào lúc khởi tạo");
        assertFalse(chatbotPanel.isVisible(), "Mới đầu chatbot phải ẩn đi");
        assertTrue(btnOpenChatbot.isVisible(), "Nút mở chat phải hiển thị");
    }

    @Test
    @DisplayName("Test luồng bấm nút mở/đóng hộp thoại Chatbot (Show/Hide)")
    void testShowAndHideChatbot() {
        controller.initialize();

        // Giả lập hành động bấm nút Open Chatbot
        controller.showChatbot(new ActionEvent());
        assertTrue(chatbotPanel.isVisible());
        assertFalse(btnOpenChatbot.isVisible());

        // Giả lập hành động bấm nút Close Chatbot
        controller.hideChatbot(new ActionEvent());
        assertFalse(chatbotPanel.isVisible());
        assertTrue(btnOpenChatbot.isVisible());
    }

    @Test
    @DisplayName("Test luồng gửi tin nhắn trống hoặc gửi tin nhắn hợp lệ kích hoạt Thread nền")
    void testSendChatbotMessage_Scenarios() throws Exception {
        controller.initialize();

        // Kịch bản 1: Ô nhập trống rỗng -> Không xử lý, không thêm bubble chat mới
        txtChatbotInput.setText("   ");
        int initialMessagesSize = chatMessages.getChildren().size();
        controller.handleSendChatbotMessage(new ActionEvent());
        assertEquals(initialMessagesSize, chatMessages.getChildren().size());

        // Kịch bản 2: Ô nhập nội dung hợp lệ -> Chặn đứng hàm ask của Python bằng MockStatic để tránh chạy ngầm thật
        try (MockedStatic<PythonChatbotConnection> mockedStaticConn = mockStatic(PythonChatbotConnection.class)) {
            PythonChatbotConnection mockConn = mock(PythonChatbotConnection.class);
            when(mockConn.ask("Xin chào Bot")).thenReturn("Phản hồi giả lập");
            mockedStaticConn.when(PythonChatbotConnection::getInstance).thenReturn(mockConn);

            txtChatbotInput.setText("Xin chào Bot");
            controller.handleSendChatbotMessage(new ActionEvent());

            // Ô nhập liệu phải được clear sạch ngay lập tức để người dùng gõ câu tiếp theo
            assertEquals("", txtChatbotInput.getText());

            // Chờ một chút xíu mili giây để luồng daemon background thread chạy xong tác vụ hoàn tất coverage
            Thread.sleep(150);
        }
    }
}