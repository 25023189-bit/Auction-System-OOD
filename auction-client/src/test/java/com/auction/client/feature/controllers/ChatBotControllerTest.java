package com.auction.client.feature.controllers;

import com.auction.client.AI.chatbot.PythonChatbotConnection;
import com.auction.client.feature.controllers.assistant.chatbot.ChatBotController;
import com.auction.client.feature.controllers.assistant.chatbot.request.ChatbotRequestRunner;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatBotControllerTest {

    private PythonChatbotConnection chatbotConnection;
    private ChatBotController controller;
    private VBox chatMessages;
    private ScrollPane chatScrollPane;
    private TextField txtChatbotInput;
    private Button btnSendChatbot;

    @BeforeAll
    static void initJavaFXToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        chatbotConnection = mock(PythonChatbotConnection.class);
        controller = new ChatBotController();
        chatMessages = new VBox();
        chatScrollPane = new ScrollPane(chatMessages);
        txtChatbotInput = new TextField();
        btnSendChatbot = new Button();

        injectField(controller, "chatMessages", chatMessages);
        injectField(controller, "chatScrollPane", chatScrollPane);
        injectField(controller, "txtChatbotInput", txtChatbotInput);
        injectField(controller, "btnSendChatbot", btnSendChatbot);
    }

    @Test
    void initializeAddsWelcomeMessageAndWiresInput() {
        runOnFxAndWait(controller::initialize);

        assertFalse(chatMessages.getChildren().isEmpty());
        assertNotNull(txtChatbotInput.getOnKeyPressed());
        assertNotNull(btnSendChatbot.getOnAction());
    }

    @Test
    void sendChatbotMessageRendersUserAndBotMessages() throws Exception {
        when(chatbotConnection.ask("Xin chào Bot")).thenReturn("Phản hồi giả lập");

        runOnFxAndWait(controller::initialize);
        injectField(controller, "requestRunner", new ChatbotRequestRunner() {
            @Override
            public void askAsync(String message, Consumer<String> onSuccess, Consumer<Throwable> onFailed) {
                chatbotConnection.ask(message);
                onSuccess.accept("Pháº£n há»“i giáº£ láº­p");
            }
        });
        int initialMessagesSize = chatMessages.getChildren().size();

        runOnFxAndWait(() -> {
            txtChatbotInput.setText("Xin chào Bot");
            controller.handleSendChatbotMessage(new ActionEvent());
        });

        verify(chatbotConnection, timeout(10000)).ask("Xin chào Bot");
        waitForFxEvents();

        assertEquals("", txtChatbotInput.getText());
        assertFalse(txtChatbotInput.isDisable());
        assertEquals(initialMessagesSize + 2, chatMessages.getChildren().size());
    }

    @Test
    void chatbotFrontendFilesLoadIndependentlyForSceneBuilder() throws Exception {
        Parent iconRoot = loadFxml("/com/example/auctionprototype/fxml/icon-chatbot-view.fxml");
        Parent chatRoot = loadFxml("/com/example/auctionprototype/fxml/chatbot-chatting-view.fxml");

        assertNotNull(iconRoot);
        assertNotNull(chatRoot);
    }

    private Parent loadFxml(String resource) throws Exception {
        AtomicReference<Parent> rootRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        runOnFxAndWait(() -> {
            try {
                URL fxml = ChatBotControllerTest.class.getResource(resource);
                assertNotNull(fxml, resource + " must exist");
                rootRef.set(new FXMLLoader(fxml).load());
            } catch (Throwable error) {
                errorRef.set(error);
            }
        });

        if (errorRef.get() != null) {
            fail(errorRef.get());
        }
        return rootRef.get();
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = ChatBotController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void runOnFxAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                errorRef.set(error);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS), "JavaFX event queue must drain in time");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fail(ex);
        }
        if (errorRef.get() != null) {
            fail(errorRef.get());
        }
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(3, TimeUnit.SECONDS), "JavaFX event queue must drain in time");
    }
}
