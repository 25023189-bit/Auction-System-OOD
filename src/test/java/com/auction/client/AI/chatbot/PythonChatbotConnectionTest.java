package com.auction.client.AI.chatbot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PythonChatbotConnectionTest {

    @Test
    @DisplayName("Test hỏi câu trống rỗng phải trả về hướng dẫn")
    void testAsk_EmptyQuestion() {
        PythonChatbotConnection connection = PythonChatbotConnection.getInstance();
        String response = connection.ask("   ");
        assertTrue(response.contains("Bạn hãy nhập câu hỏi"));
    }

    @Test
    @DisplayName("Test gọi bot Python thành công và bóc tách dữ liệu JSON")
    void testAsk_Success() throws Exception {
        // FIX: Thêm Mockito.CALLS_REAL_METHODS để bảo vệ trình tải lớp hệ thống của JDK 25
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<ProcessBuilder> mockedProcessBuilder = mockConstruction(
                     ProcessBuilder.class,
                     withSettings().defaultAnswer(Answers.RETURNS_SELF),
                     (mock, context) -> {
                         Process mockProcess = mock(Process.class);
                         when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
                         when(mockProcess.exitValue()).thenReturn(0);
                         when(mock.start()).thenReturn(mockProcess);
                     })) {

            // Chỉ giả lập trúng đích các file liên quan trực tiếp tới Chatbot Python
            mockedFiles.when(() -> Files.isRegularFile(argThat(path -> path != null &&
                    (path.toString().contains("app.py") || path.toString().contains("output.json"))))).thenReturn(true);

            mockedFiles.when(() -> Files.readString(argThat(path -> path != null &&
                    path.toString().contains("output.json")), any())).thenReturn("{\"answer\":\"\\u0058\\u0069\\u006e \\u0063\\u0068\\u00e0\\u006f Hân UET!\"}");

            PythonChatbotConnection connection = PythonChatbotConnection.getInstance();
            String response = connection.ask("Hệ thống là gì?");

            assertEquals("Xin chào Hân UET!", response, "Phải giải mã unicode JSON chuẩn xác");
        }
    }

    @Test
    @DisplayName("Test kịch bản Python văng lỗi hoặc file JSON chứa Traceback lỗi")
    void testAsk_PythonErrorResponse() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<ProcessBuilder> mockedProcessBuilder = mockConstruction(
                     ProcessBuilder.class,
                     withSettings().defaultAnswer(Answers.RETURNS_SELF),
                     (mock, context) -> {
                         Process mockProcess = mock(Process.class);
                         when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
                         when(mockProcess.exitValue()).thenReturn(0);
                         when(mock.start()).thenReturn(mockProcess);
                     })) {

            mockedFiles.when(() -> Files.isRegularFile(argThat(path -> path != null &&
                    (path.toString().contains("app.py") || path.toString().contains("output.json"))))).thenReturn(true);

            mockedFiles.when(() -> Files.readString(argThat(path -> path != null &&
                    path.toString().contains("output.json")), any())).thenReturn("{\"answer\":\"Lỗi kết nối hoặc Traceback error\"}");

            PythonChatbotConnection connection = PythonChatbotConnection.getInstance();
            String response = connection.ask("Test lỗi");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test lỗi hệ thống IO hoặc sập nguồn tiến trình đột ngột")
    void testAsk_IOExceptionThrown() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            // Cho phép hệ thống nhận diện file app.py để vượt qua vòng validate cơ bản
            mockedFiles.when(() -> Files.isRegularFile(argThat(path -> path != null &&
                    path.toString().contains("app.py")))).thenReturn(true);

            // Ép riêng hàm xóa các file dữ liệu tạm của Chatbot ném ra lỗi IO
            mockedFiles.when(() -> Files.deleteIfExists(argThat(path -> path != null &&
                            (path.toString().contains("output.json") || path.toString().contains("errol_info.json")))))
                    .thenThrow(new IOException("Disk Full"));

            PythonChatbotConnection connection = PythonChatbotConnection.getInstance();
            String response = connection.ask("Crash test");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }
}