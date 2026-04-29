package com.auction.client.chatbot;

/**
 * Câu trả lời dự phòng khi chatbot Python không chạy được hoặc không trả dữ liệu hợp lệ.
 */
public final class ChatbotFallback {
    public static final String MESSAGE = "Chat hiện đang lỗi, có thể gọi đến tư vấn viên theo số 038xxxxxxx";

    private ChatbotFallback() {
    }
}
