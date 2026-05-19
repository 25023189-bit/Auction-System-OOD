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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        controller = createController();
    }

    private ChatbotController createController() throws Exception {
        ChatbotController newController = new ChatbotController();

        // Bơm các linh kiện này vào các trường private @FXML của Controller bằng Reflection
        injectField(newController, "chatbotRoot", chatbotRoot);
        injectField(newController, "chatbotPanel", chatbotPanel);
        injectField(newController, "chatMessages", chatMessages);
        injectField(newController, "chatScrollPane", chatScrollPane);
        injectField(newController, "txtChatbotInput", txtChatbotInput);
        injectField(newController, "btnOpenChatbot", btnOpenChatbot);
        injectField(newController, "btnCloseChatbot", btnCloseChatbot);
        injectField(newController, "btnSendChatbot", btnSendChatbot);

        return newController;
    }

    private void injectField(ChatbotController target, String fieldName, Object value) throws Exception {
        Field field = ChatbotController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX event queue must drain in time");
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
        // Kịch bản 2: Ô nhập nội dung hợp lệ -> Chặn đứng hàm ask của Python bằng MockStatic để tránh chạy ngầm thật
        try (MockedStatic<PythonChatbotConnection> mockedStaticConn = mockStatic(PythonChatbotConnection.class)) {
            PythonChatbotConnection mockConn = mock(PythonChatbotConnection.class);
            CountDownLatch askStarted = new CountDownLatch(1);
            CountDownLatch releaseAnswer = new CountDownLatch(1);

            when(mockConn.ask("Xin chào Bot")).thenAnswer(invocation -> {
                askStarted.countDown();
                assertTrue(releaseAnswer.await(2, TimeUnit.SECONDS), "Test must release mocked chatbot answer");
                return "Phản hồi giả lập";
            });
            mockedStaticConn.when(PythonChatbotConnection::getInstance).thenReturn(mockConn);

            controller = createController();
            controller.initialize();

            // Kịch bản 1: Ô nhập trống rỗng -> Không xử lý, không thêm bubble chat mới
            txtChatbotInput.setText("   ");
            int initialMessagesSize = chatMessages.getChildren().size();
            controller.handleSendChatbotMessage(new ActionEvent());
            assertEquals(initialMessagesSize, chatMessages.getChildren().size());
            verifyNoInteractions(mockConn);

            txtChatbotInput.setText("Xin chào Bot");
            controller.handleSendChatbotMessage(new ActionEvent());

            // Ô nhập liệu phải được clear sạch ngay lập tức để người dùng gõ câu tiếp theo
            assertEquals("", txtChatbotInput.getText());
            assertTrue(askStarted.await(2, TimeUnit.SECONDS), "Chatbot request must run on background thread");
            assertTrue(txtChatbotInput.isDisable(), "Input must be disabled while waiting for chatbot");

            releaseAnswer.countDown();
            verify(mockConn, timeout(2000)).ask("Xin chào Bot");
            waitForFxEvents();

            assertFalse(txtChatbotInput.isDisable(), "Input must be re-enabled after chatbot response");
            assertEquals(initialMessagesSize + 2, chatMessages.getChildren().size());
        }
    }
}
