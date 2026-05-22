package com.auction.client.feature.controllers.assistant.chatbot.request;

import com.auction.client.AI.chatbot.PythonChatbotConnection;
import javafx.concurrent.Task;

public class ChatbotRequestRunner {
    private final PythonChatbotConnection chatbotConnection;

    public ChatbotRequestRunner(PythonChatbotConnection chatbotConnection) {
        this.chatbotConnection = chatbotConnection;
    }

    public Task<String> createTask(String userMessage) {
        return new Task<>() {
            @Override
            protected String call() {
                return chatbotConnection.ask(userMessage);
            }
        };
    }
}
