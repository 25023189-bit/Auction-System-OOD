package com.auction.client.AI.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PythonChatbotConnection {
    private static final PythonChatbotConnection INSTANCE = new PythonChatbotConnection();
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(90);
    private static final String CHATBOT_DIR_PROPERTY = "auction.chatbot.dir";
    private static final String CHATBOT_DIR_ENV = "AUCTION_CHATBOT_DIR";
    private static final String PYTHON_COMMAND_PROPERTY = "auction.chatbot.python";
    private static final String PYTHON_COMMAND_ENV = "AUCTION_CHATBOT_PYTHON";

    private final BotSpec chatbot;
    private final String configuredPythonCommand;

    private PythonChatbotConnection() {
        this.chatbot = new BotSpec(
                resolveChatbotDirectory(),
                Path.of("IOdata").resolve("output.json"),
                Path.of("status").resolve("infomation.json"),
                "answer"
        );
        this.configuredPythonCommand = resolvePythonCommand();
    }

    public static PythonChatbotConnection getInstance() {
        return INSTANCE;
    }

    public synchronized String ask(String question) {
        String normalizedQuestion = question == null ? "" : question.trim();
        if (normalizedQuestion.isEmpty()) {
            return "Bạn hãy nhập câu hỏi về hệ thống đấu giá để chatbot hỗ trợ.";
        }

        boolean interrupted = false;
        Optional<String> answer = Optional.empty();

        try {
            answer = askBot(normalizedQuestion);
        } catch (InterruptedException ex) {
            interrupted = true;
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return answer.orElse(ChatbotFallback.MESSAGE);
    }

    public String sendQuery(String query) {
        return ask(query);
    }

    private Optional<String> askBot(String question) throws InterruptedException {
        try {
            validateBotFiles();
            Files.deleteIfExists(chatbot.outputPath());
            Files.deleteIfExists(chatbot.errorInfoPath());

            if (!runPythonBot(question)) {
                writeClientErrorInfo("Python chatbot process failed. See " + chatbot.processLogPath());
                return Optional.empty();
            }

            Optional<String> answer = readJsonStringField(chatbot.outputPath(), chatbot.outputField());
            if (answer.isEmpty()) {
                answer = readJsonStringField(chatbot.outputPath(), "response");
            }
            if (answer.isEmpty()) {
                answer = readJsonStringField(chatbot.outputPath(), "message");
            }
            answer = answer.map(String::trim).filter(value -> !value.isEmpty());

            if (answer.isEmpty() || looksLikeErrorAnswer(answer.get())) {
                Optional<String> fallbackAnswer = readJsonStringField(chatbot.informationPath(), "fallback_answer")
                        .map(String::trim)
                        .filter(value -> !value.isEmpty());
                return fallbackAnswer.filter(value -> !looksLikeErrorAnswer(value));
            }

            return answer;
        } catch (IOException | RuntimeException ex) {
            writeClientErrorInfo(ex.getMessage());
            return Optional.empty();
        }
    }

    private void validateBotFiles() throws IOException {
        if (!Files.isRegularFile(chatbot.appPath())) {
            throw new IOException("Missing Python chatbot entrypoint: " + chatbot.appPath());
        }
        Files.createDirectories(chatbot.outputPath().getParent());
        Files.createDirectories(chatbot.errorInfoPath().getParent());
    }

    private boolean runPythonBot(String question) throws InterruptedException {
        for (String[] command : pythonCommands()) {
            if (runPythonCommand(question, command) == RunStatus.SUCCESS) {
                return true;
            }
        }

        return false;
    }

    private List<String[]> pythonCommands() {
        List<String[]> commands = new ArrayList<>();
        if (configuredPythonCommand != null && !configuredPythonCommand.isBlank()) {
            commands.add(new String[]{configuredPythonCommand.trim()});
        }
        commands.add(new String[]{"python"});
        commands.add(new String[]{"python3"});
        commands.add(new String[]{"py", "-3"});
        return commands;
    }

    private static String resolvePythonCommand() {
        String configured = System.getProperty(PYTHON_COMMAND_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(PYTHON_COMMAND_ENV);
        }
        return configured;
    }

    private RunStatus runPythonCommand(String question, String... commandPrefix) throws InterruptedException {
        Process process;
        try {
            process = startPythonProcess(question, commandPrefix);
        } catch (IOException ex) {
            return RunStatus.START_FAILED;
        }

        try {
            if (!process.waitFor(PROCESS_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return RunStatus.FAILED;
            }
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            throw ex;
        }

        return process.exitValue() == 0 ? RunStatus.SUCCESS : RunStatus.FAILED;
    }

    private Process startPythonProcess(String question, String... commandPrefix) throws IOException {
        String[] command = new String[commandPrefix.length + 3];
        System.arraycopy(commandPrefix, 0, command, 0, commandPrefix.length);
        command[commandPrefix.length] = chatbot.appPath().getFileName().toString();
        command[commandPrefix.length + 1] = "--question";
        command[commandPrefix.length + 2] = question;

        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(chatbot.appDirectory().toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.to(chatbot.processLogPath().toFile()));
        processBuilder.environment().put("PYTHONUTF8", "1");
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        return processBuilder.start();
    }

    private Optional<String> readJsonStringField(Path path, String field) throws IOException {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }

        String output = Files.readString(path, StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(unescapeJson(matcher.group(1)));
    }

    private boolean looksLikeErrorAnswer(String answer) {
        String normalized = Normalizer.normalize(answer, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.startsWith("loi:")
                || normalized.startsWith("error:")
                || normalized.contains("khong goi duoc ollama")
                || normalized.contains("khong the ket noi toi llm")
                || normalized.contains("object has no attribute")
                || normalized.contains("attributeerror")
                || normalized.contains("modulenotfounderror")
                || normalized.contains("importerror")
                || normalized.contains("permission denied")
                || normalized.contains("traceback");
    }

    private Path resolveChatbotDirectory() {
        String configured = System.getProperty(CHATBOT_DIR_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(CHATBOT_DIR_ENV);
        }
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }

        Path projectRoot = locateProjectRoot();
        return projectRoot.resolve("Auction_AI").resolve("ChatBot");
    }

    private Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            Path chatbotEntrypoint = current.resolve("Auction_AI").resolve("ChatBot").resolve("Main").resolve("app.py");
            if (Files.isRegularFile(chatbotEntrypoint)) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private void writeClientErrorInfo(String message) {
        try {
            Files.createDirectories(chatbot.errorInfoPath().getParent());
            String safeMessage = message == null || message.isBlank()
                    ? "Python chatbot is not available on this machine."
                    : message;
            String payload = """
                    {
                      "status": "error",
                      "source": "java-client",
                      "message": "%s"
                    }
                    """.formatted(escapeJson(safeMessage));
            Files.writeString(chatbot.errorInfoPath(), payload, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // The UI already returns a fallback answer; diagnostics are best-effort only.
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

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

    private record BotSpec(
            Path directory,
            Path relativeOutputPath,
            Path relativeInformationPath,
            String outputField
    ) {
        private Path appPath() {
            return appDirectory().resolve("app.py");
        }

        private Path appDirectory() {
            return directory.resolve("Main");
        }

        private Path outputPath() {
            return directory.resolve(relativeOutputPath);
        }

        private Path informationPath() {
            return directory.resolve(relativeInformationPath);
        }

        private Path errorInfoPath() {
            return directory.resolve("status").resolve("errol_info.json");
        }

        private Path processLogPath() {
            return directory.resolve("status").resolve("chatbot_process.log");
        }
    }

    private enum RunStatus {
        SUCCESS,
        START_FAILED,
        FAILED
    }
}
