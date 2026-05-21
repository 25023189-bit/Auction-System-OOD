package com.auction.client.AI.chatbot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    @DisplayName("Test gọi bot Python thành công và bóc tách JSON có unicode/escape")
    void testAsk_SuccessWithEscapedUnicodeJson() throws Exception {
        String outputJson = "{\"answer\":\"Xin chào Hân UET!\\nDòng 2: \\\"bid\\\" \\\\ path \\u0111ấu giá\"}";

        try (MockedStatic<Files> mockedFiles = mockChatbotFiles(outputJson, true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            PythonChatbotConnection connection = PythonChatbotConnection.getInstance();
            String response = connection.ask("Hệ thống là gì?");

            assertEquals("Xin chào Hân UET!\nDòng 2: \"bid\" \\ path đấu giá", response);
        }
    }

    @Test
    @DisplayName("Test đọc fallback field response khi output không có answer")
    void testAsk_ReadsResponseFieldWhenAnswerMissing() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"response\":\"Trả lời từ response\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Câu hỏi hợp lệ");

            assertEquals("Trả lời từ response", response);
        }
    }

    @Test
    @DisplayName("Test đọc fallback field message khi output không có answer/response")
    void testAsk_ReadsMessageFieldWhenAnswerAndResponseMissing() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"message\":\"Trả lời từ message\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Câu hỏi hợp lệ");

            assertEquals("Trả lời từ message", response);
        }
    }

    @Test
    @DisplayName("Test output JSON thiếu schema hợp lệ phải trả fallback")
    void testAsk_InvalidOutputSchemaReturnsFallback() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"unexpected\":\"value\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Câu hỏi hợp lệ");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test process thành công nhưng không tạo output.json phải trả fallback")
    void testAsk_MissingOutputFileReturnsFallback() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles(null, false);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Câu hỏi hợp lệ");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test kịch bản Python trả lỗi hoặc JSON chứa Traceback")
    void testAsk_PythonErrorResponse() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Error: Traceback lỗi runtime\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Test lỗi");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test technical Python model error in message field returns fallback")
    void testAsk_PythonTechnicalMessageReturnsFallback() throws Exception {
        String outputJson = "{\"status\":\"error\",\"message\":\"'LogisticRegression' object has no attribute 'multi_class'\"}";

        try (MockedStatic<Files> mockedFiles = mockChatbotFiles(outputJson, true);
             MockedConstruction<ProcessBuilder> ignored = mockSuccessfulPythonProcess()) {

            String response = PythonChatbotConnection.getInstance().ask("Test model error");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test process Python timeout returns fallback")
    void testAsk_ProcessTimeoutReturnsFallback() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Không được đọc\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockPythonProcess(false, 0)) {

            String response = PythonChatbotConnection.getInstance().ask("Timeout test");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test process Python exit code lỗi phải trả fallback")
    void testAsk_ProcessNonZeroExitReturnsFallback() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Không được đọc\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockPythonProcess(true, 1)) {

            String response = PythonChatbotConnection.getInstance().ask("Exit code test");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    @Test
    @DisplayName("Test python start fail nhưng py -3 thành công")
    void testAsk_FallsBackToPyLauncherWhenPythonCommandCannotStart() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Trả lời sau khi dùng py launcher\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockConstruction(
                     ProcessBuilder.class,
                     withSettings().defaultAnswer(Answers.RETURNS_SELF),
                     (mock, context) -> {
                         when(mock.environment()).thenReturn(new HashMap<>());
                         int attempt = attempts.incrementAndGet();
                         if (attempt == 1) {
                             when(mock.start()).thenThrow(new IOException("python command missing"));
                             return;
                         }
                         Process mockProcess = mock(Process.class);
                         when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
                         when(mockProcess.exitValue()).thenReturn(0);
                         when(mock.start()).thenReturn(mockProcess);
                     })) {

            String response = PythonChatbotConnection.getInstance().ask("Fallback command test");

            assertEquals("Trả lời sau khi dùng py launcher", response);
            assertEquals(2, attempts.get(), "Must try python first, then py -3");
        }
    }

    @Test
    @DisplayName("Test cả python và py -3 đều không start được phải trả fallback")
    void testAsk_BothPythonCommandsCannotStartReturnsFallback() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Không được đọc\"}", true);
             MockedConstruction<ProcessBuilder> ignored = mockConstruction(
                     ProcessBuilder.class,
                     withSettings().defaultAnswer(Answers.RETURNS_SELF),
                     (mock, context) -> {
                         when(mock.environment()).thenReturn(new HashMap<>());
                         attempts.incrementAndGet();
                         when(mock.start()).thenThrow(new IOException("command missing"));
                     })) {

            String response = PythonChatbotConnection.getInstance().ask("No command test");

            assertEquals(ChatbotFallback.MESSAGE, response);
            assertEquals(3, attempts.get(), "Must try python, python3, and py -3 launch commands");
        }
    }

    @Test
    @DisplayName("Test lỗi IO khi chuẩn bị file runtime phải trả fallback")
    void testAsk_IOExceptionThrown() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockChatbotFiles("{\"answer\":\"Không được đọc\"}", true)) {
            mockedFiles.when(() -> Files.deleteIfExists(argThat(PythonChatbotConnectionTest::isRuntimeFile)))
                    .thenThrow(new IOException("Disk Full"));

            String response = PythonChatbotConnection.getInstance().ask("Crash test");

            assertEquals(ChatbotFallback.MESSAGE, response);
        }
    }

    private static MockedStatic<Files> mockChatbotFiles(String outputJson, boolean outputExists) throws IOException {
        MockedStatic<Files> mockedFiles = mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);

        mockedFiles.when(() -> Files.isRegularFile(argThat(PythonChatbotConnectionTest::isAppPath)))
                .thenReturn(true);
        mockedFiles.when(() -> Files.isRegularFile(argThat(PythonChatbotConnectionTest::isOutputPath)))
                .thenReturn(outputExists);
        mockedFiles.when(() -> Files.isRegularFile(argThat(PythonChatbotConnectionTest::isInformationPath)))
                .thenReturn(false);
        mockedFiles.when(() -> Files.createDirectories(argThat(PythonChatbotConnectionTest::isRuntimeDirectory)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        mockedFiles.when(() -> Files.deleteIfExists(argThat(PythonChatbotConnectionTest::isRuntimeFile)))
                .thenReturn(false);

        if (outputJson != null) {
            mockedFiles.when(() -> Files.readString(argThat(PythonChatbotConnectionTest::isOutputPath), any()))
                    .thenReturn(outputJson);
        }

        return mockedFiles;
    }

    private static MockedConstruction<ProcessBuilder> mockSuccessfulPythonProcess() {
        return mockPythonProcess(true, 0);
    }

    private static MockedConstruction<ProcessBuilder> mockPythonProcess(boolean completed, int exitCode) {
        return mockConstruction(
                ProcessBuilder.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, context) -> {
                    Process mockProcess = mock(Process.class);
                    when(mock.environment()).thenReturn(new HashMap<>());
                    when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(completed);
                    when(mockProcess.exitValue()).thenReturn(exitCode);
                    when(mockProcess.destroyForcibly()).thenReturn(mockProcess);
                    when(mock.start()).thenReturn(mockProcess);
                }
        );
    }

    private static boolean isAppPath(Path path) {
        return path != null && normalized(path).endsWith("Auction_AI/ChatBot/Main/app.py");
    }

    private static boolean isOutputPath(Path path) {
        return path != null && normalized(path).endsWith("Auction_AI/ChatBot/IOdata/output.json");
    }

    private static boolean isInformationPath(Path path) {
        return path != null && normalized(path).endsWith("Auction_AI/ChatBot/status/infomation.json");
    }

    private static boolean isRuntimeFile(Path path) {
        if (path == null) {
            return false;
        }
        String value = normalized(path);
        return value.endsWith("Auction_AI/ChatBot/IOdata/output.json")
                || value.endsWith("Auction_AI/ChatBot/status/errol_info.json");
    }

    private static boolean isRuntimeDirectory(Path path) {
        if (path == null) {
            return false;
        }
        String value = normalized(path);
        return value.endsWith("Auction_AI/ChatBot/IOdata")
                || value.endsWith("Auction_AI/ChatBot/status");
    }

    private static String normalized(Path path) {
        return path.toAbsolutePath().normalize().toString().replace('\\', '/');
    }
}
