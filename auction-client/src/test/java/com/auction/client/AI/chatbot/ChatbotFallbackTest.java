package com.auction.client.AI.chatbot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ChatbotFallbackTest {

    @Test
    @DisplayName("Test hằng số thông báo và constructor private của ChatbotFallback")
    void testFallbackConstantAndConstructor() throws Exception {
        // Kiểm tra hằng số phản hồi mặc định
        assertNotNull(ChatbotFallback.MESSAGE);
        assertTrue(ChatbotFallback.MESSAGE.contains("Chatbot hiện chưa phản hồi được"));

        // Phủ xanh constructor private bằng Reflection
        Constructor<ChatbotFallback> constructor = ChatbotFallback.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ChatbotFallback instance = constructor.newInstance();
        assertNotNull(instance);
    }
}