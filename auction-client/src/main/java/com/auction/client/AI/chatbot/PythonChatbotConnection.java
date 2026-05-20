package com.auction.client.AI.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Lớp điều phối luồng trao đổi thông tin tệp tin giữa ứng dụng JavaFX Client và Script Python AI.
 * Thiết kế theo mẫu Singleton để tập trung quản lý tránh xung đột tranh chấp quyền ghi file từ OS.
 */
public final class PythonChatbotConnection {

    private static final PythonChatbotConnection INSTANCE = new PythonChatbotConnection();
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(90);

    // Xác định tên tệp tin đầu vào/đầu ra tạm thời tại thư mục gốc của dự án chạy máy trạm
    private final Path inputPath = Path.of("chatbot_input.txt");
    private final Path outputPath = Path.of("chatbot_output.txt");

    private PythonChatbotConnection() {
        // Khóa Constructor để ngăn cản việc khởi tạo tự do ngoài luồng điều phối
    }

    public static PythonChatbotConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Đồng bộ hóa chuỗi tin nhắn sang tệp tin văn bản, đợi phản hồi từ tiến trình Python biên dịch.
     * * @param query Nội dung câu hỏi gốc của người dùng hệ thống.
     * @return Chuỗi văn bản phản hồi đã lọc sạch hoặc chuỗi Fallback an toàn.
     */
    public String sendQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ChatbotFallback.MESSAGE;
        }

        try {
            // Thực hiện ghi đè nội dung câu hỏi mới xuống ổ đĩa, chỉ định tường minh bảng mã UTF-8
            Files.writeString(inputPath, query, StandardCharsets.UTF_8);

            long startTimestamp = System.currentTimeMillis();
            long timeoutLimitMillis = PROCESS_TIMEOUT.toMillis();

            // Vòng lặp Polling có kiểm soát thời gian chết để chờ đợi Script AI của Python sinh kết quả trả về
            while (System.currentTimeMillis() - startTimestamp < timeoutLimitMillis) {
                if (Files.exists(outputPath)) {
                    // Trì hoãn 100ms ngắn ngủi đảm bảo hệ điều hành hoàn thành hoàn toàn luồng ghi xuống ổ đĩa vật lý
                    TimeUnit.MILLISECONDS.sleep(100);

                    // Đọc nội dung thô và cắt bỏ các khoảng trắng thừa ở hai đầu chuỗi văn bản
                    String completeResult = Files.readString(outputPath, StandardCharsets.UTF_8).trim();

                    // Tiến hành dọn dẹp các tệp tin trung gian nhằm tránh rác dữ liệu ở lượt truy vấn tiếp theo
                    Files.deleteIfExists(outputPath);
                    Files.deleteIfExists(inputPath);

                    if (!completeResult.isEmpty()) {
                        return completeResult;
                    }
                }
                // Chu kỳ nghỉ (200ms) giữa các lần rà soát File để đưa mức chiếm dụng CPU của máy trạm về ~0%
                TimeUnit.MILLISECONDS.sleep(200);
            }

        } catch (IOException | InterruptedException exception) {
            // Thiết lập lại trạng thái ngắt luồng chuẩn của Thread hiện thời nếu tiến trình bị ngắt ép buộc
            Thread.currentThread().interrupt();
            return ChatbotFallback.MESSAGE;
        } finally {
            // Giải phóng file input tại khối lệnh bọc cuối cùng nếu xảy ra lỗi hết thời gian chờ (Timeout)
            try {
                Files.deleteIfExists(inputPath);
            } catch (IOException ignored) {
                // Bỏ qua ngoại lệ phụ phát sinh trong giai đoạn dọn dẹp khẩn cấp
            }
        }

        return ChatbotFallback.MESSAGE;
    }
}