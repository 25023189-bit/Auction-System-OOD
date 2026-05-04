package com.auction.client.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cầu nối singleton giữa JavaFX client và chatbot Python.
 *
 * Vai trò:
 * - Ghi câu hỏi vào input.json, chạy app.py và đọc câu trả lời từ output.json.
 * - Cung cấp timeout/fallback để lỗi Python không làm treo client.
 *
 * Luồng chính:
 * 1. ChatbotController gọi ask(question) từ background task.
 * 2. Connection validate file, ghi input, chạy Python, đọc response và trả text hoặc fallback.
 *
 * Business rules:
 * - Mỗi lần chỉ xử lý một câu hỏi để tránh ghi đè input/output của chatbot.
 * - Nếu thiếu file, process lỗi, timeout hoặc output không có response thì trả ChatbotFallback.MESSAGE.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe cho ask(): method synchronized bảo vệ cặp file input/output dùng chung.
 * - Dependency: Files/Path, ProcessBuilder, Pattern, Duration, ChatbotFallback.
 */
public final class PythonChatbotConnection {
    private static final PythonChatbotConnection INSTANCE = new PythonChatbotConnection();
    // Giới hạn thời gian để process Python lỗi không làm treo client.
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(20);
    // Regex tối giản để lấy trường "response" từ output JSON do chatbot sinh ra.
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("\"response\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);

    private final Path chatbotDirectory;
    private final Path inputPath;
    private final Path outputPath;
    private final Path appPath;

    private PythonChatbotConnection() {
        this.chatbotDirectory = locateChatbotDirectory();
        this.inputPath = chatbotDirectory.resolve("data").resolve("input.json");
        this.outputPath = chatbotDirectory.resolve("data").resolve("output.json");
        this.appPath = chatbotDirectory.resolve("app.py");
    }

    public static PythonChatbotConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Gửi một câu hỏi tới chatbot.
     * synchronized để tránh hai request ghi đè cùng file input/output.
     */
    public synchronized String ask(String question) {
        try {
            validatePythonChatbotFiles();
            writeQuestion(question);
            Files.deleteIfExists(outputPath);
            runPythonChatbot();
            return readAnswer().orElse(ChatbotFallback.MESSAGE);
        } catch (IOException | InterruptedException | RuntimeException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return ChatbotFallback.MESSAGE;
        }
    }

    // Kiểm tra entrypoint Python và tạo thư mục data nếu chưa có.
    private void validatePythonChatbotFiles() throws IOException {
        if (!Files.isRegularFile(appPath)) {
            throw new IOException("Missing Python chatbot entrypoint: " + appPath);
        }
        Files.createDirectories(inputPath.getParent());
        Files.createDirectories(outputPath.getParent());
    }

    // Ghi câu hỏi theo format JSON mà app.py đang đọc.
    private void writeQuestion(String question) throws IOException {
        String payload = "{\n  \"message\": \"" + escapeJson(question) + "\"\n}\n";
        Files.writeString(inputPath, payload, StandardCharsets.UTF_8);
    }

    // Thử cả lệnh python và py -3 để chạy được trên nhiều máy Windows.
    private void runPythonChatbot() throws IOException, InterruptedException {
        if (runPythonCommand("python")) {
            return;
        }

        if (runPythonCommand("py", "-3")) {
            return;
        }

        throw new IOException("Python chatbot failed.");
    }

    // Trả về false nếu command không tồn tại, timeout hoặc process thoát lỗi.
    private boolean runPythonCommand(String... commandPrefix) throws InterruptedException {
        Process process;
        try {
            process = startPythonProcess(commandPrefix);
        } catch (IOException ex) {
            return false;
        }

        if (!process.waitFor(PROCESS_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return false;
        }

        return process.exitValue() == 0;
    }

    // Process chạy trong thư mục chatbot để các đường dẫn tương đối của Python vẫn đúng.
    private Process startPythonProcess(String... commandPrefix) throws IOException {
        String[] command = new String[commandPrefix.length + 1];
        System.arraycopy(commandPrefix, 0, command, 0, commandPrefix.length);
        command[command.length - 1] = appPath.getFileName().toString();

        return new ProcessBuilder(command)
                .directory(chatbotDirectory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    // Đọc output.json và chỉ trả lời khi trường response hợp lệ.
    private Optional<String> readAnswer() throws IOException {
        if (!Files.isRegularFile(outputPath)) {
            return Optional.empty();
        }

        String output = Files.readString(outputPath, StandardCharsets.UTF_8);
        Matcher matcher = RESPONSE_PATTERN.matcher(output);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String response = unescapeJson(matcher.group(1)).trim();
        return response.isEmpty() ? Optional.empty() : Optional.of(response);
    }

    // Tìm thư mục Auction_AI/ChatBot bằng cách đi ngược từ thư mục chạy hiện tại.
    private Path locateChatbotDirectory() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve("Auction_AI").resolve("ChatBot");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return Path.of("Auction_AI", "ChatBot").toAbsolutePath().normalize();
    }

    // Escape thủ công vì payload nhỏ và chỉ cần ghi một trường message.
    private String escapeJson(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        return builder.toString();
    }

    // Chuyển chuỗi JSON escaped về text thường để hiển thị trong bubble chat.
    private String unescapeJson(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character != '\\' || i + 1 >= value.length()) {
                builder.append(character);
                continue;
            }

            char escaped = value.charAt(++i);
            switch (escaped) {
                case '"' -> builder.append('"');
                case '\\' -> builder.append('\\');
                case '/' -> builder.append('/');
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> {
                    if (i + 4 >= value.length()) {
                        builder.append("\\u");
                        continue;
                    }
                    String hex = value.substring(i + 1, i + 5);
                    builder.append((char) Integer.parseInt(hex, 16));
                    i += 4;
                }
                default -> builder.append(escaped);
            }
        }
        return builder.toString();
    }
}
