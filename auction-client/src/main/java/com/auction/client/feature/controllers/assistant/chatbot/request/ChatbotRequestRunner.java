package com.auction.client.feature.controllers.assistant.chatbot.request;

import com.auction.client.AI.chatbot.PythonChatbotConnection;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class ChatbotRequestRunner {
    private final PythonChatbotConnection chatbotConnection;

    public ChatbotRequestRunner() {
        this(PythonChatbotConnection.getInstance());
    }

    public ChatbotRequestRunner(PythonChatbotConnection chatbotConnection) {
        this.chatbotConnection = chatbotConnection;
    }

    public void askAsync(String message, Consumer<String> onSuccess, Consumer<Throwable> onFailed) {
        Task<String> chatbotTask = new Task<>() {
            @Override
            protected String call() {
                return chatbotConnection.ask(message);
            }
        };

        chatbotTask.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.accept(chatbotTask.getValue());
            }
        });
        chatbotTask.setOnFailed(event -> {
            if (onFailed != null) {
                onFailed.accept(chatbotTask.getException());
            }
        });

        Thread backgroundThread = new Thread(chatbotTask, "auction-chatbot-request");
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}
