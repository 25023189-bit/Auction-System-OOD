package com.auction.client.AI.chatbot;

/**
 * Hằng số phản hồi dự phòng cho chatbot.
 *
 * Vai trò:
 * - Cung cấp nội dung trả lời thống nhất khi chatbot Python không khả dụng.
 * - Tránh để UI chatbot hiển thị lỗi kỹ thuật thô cho người dùng.
 *
 * Luồng chính:
 * 1. PythonChatbotConnection hoặc ChatbotController phát hiện lỗi/response rỗng.
 * 2. Caller dùng MESSAGE làm phản hồi bot trên giao diện.
 *
 * Business rules:
 * - Fallback phải là thông báo thân thiện và có hướng xử lý cho người dùng.
 * - Lớp tiện ích không được khởi tạo instance.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: immutable constant, constructor private.
 * - Dependency: Không phụ thuộc ngoài.
 */
public final class ChatbotFallback {
    public static final String MESSAGE = "Chatbot hiện chưa phản hồi được. Bạn có thể hỏi lại về đăng ký, đăng nhập, tìm phiên, đặt giá hoặc số dư.";

    private ChatbotFallback() {
    }
}
