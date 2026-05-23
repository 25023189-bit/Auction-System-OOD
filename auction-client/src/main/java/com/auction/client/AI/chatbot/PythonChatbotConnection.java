package com.auction.client.AI.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonChatbotConnection.class);

    private static final PythonChatbotConnection INSTANCE = new PythonChatbotConnection();
    private static final String DEFAULT_CHATBOT_DIR = "Auction_AI/ChatBot";
    private static final long DEFAULT_TIMEOUT_SECONDS = 10L;
    private static final int MAX_LOG_CHARS = 1_000;
    private static final String CHATBOT_DIR_PROPERTY = "auction.chatbot.dir";
    private static final String CHATBOT_DIR_ENV = "AUCTION_CHATBOT_DIR";
    private static final String PYTHON_COMMAND_PROPERTY = "auction.chatbot.python";
    private static final String PYTHON_COMMAND_ENV = "AUCTION_PYTHON_COMMAND";
    private static final String TIMEOUT_SECONDS_PROPERTY = "auction.chatbot.timeoutSeconds";
    private static final String TIMEOUT_SECONDS_ENV = "AUCTION_CHATBOT_TIMEOUT_SECONDS";

    private final BotSpec chatbot;
    private final String pythonCommand;
    private final Duration processTimeout;

    private PythonChatbotConnection() {
        this.chatbot = new BotSpec(
                resolveChatbotDirectory(),
                Path.of("IOdata").resolve("input.json"),
                Path.of("IOdata").resolve("output.json"),
                Path.of("status").resolve("infomation.json"),
                "answer"
        );
        this.pythonCommand = resolvePythonCommand();
        this.processTimeout = resolveTimeout();
        logConfiguration();
        validatePaths();
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
            if (!validatePaths()) {
                LOGGER.warn("ChatBot path diagnostics found issues. Continuing with existing chatbot fallback behavior.");
            }
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
            LOGGER.warn("ChatBot request failed. Returning fallback answer. reason={}", ex.getMessage(), ex);
            writeClientErrorInfo(ex.getMessage());
            return Optional.empty();
        }
    }

    private void validateBotFiles() throws IOException {
        if (!Files.isRegularFile(chatbot.appPath())) {
            LOGGER.warn("ChatBot script path is missing or is not a file: {}", chatbot.appPath());
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
        if (pythonCommand != null && !pythonCommand.isBlank()) {
            commands.add(new String[]{pythonCommand.trim()});
        }
        addIfMissing(commands, new String[]{"python"});
        commands.add(new String[]{"python3"});
        commands.add(new String[]{"py", "-3"});
        return commands;
    }

    private void addIfMissing(List<String[]> commands, String[] command) {
        for (String[] existing : commands) {
            if (String.join(" ", existing).equals(String.join(" ", command))) {
                return;
            }
        }
        commands.add(command);
    }

    private static String resolvePythonCommand() {
        String configured = System.getProperty(PYTHON_COMMAND_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            LOGGER.info("ChatBot Python command resolved from system property {}: {}", PYTHON_COMMAND_PROPERTY, configured);
            return configured;
        }

        configured = System.getenv(PYTHON_COMMAND_ENV);
        if (configured != null && !configured.isBlank()) {
            LOGGER.info("ChatBot Python command resolved from environment variable {}: {}", PYTHON_COMMAND_ENV, configured);
            return configured;
        }

        LOGGER.info("ChatBot Python command using default: python");
        return "python";
    }

    private RunStatus runPythonCommand(String question, String... commandPrefix) throws InterruptedException {
        Process process;
        String[] command = buildCommand(question, commandPrefix);
        List<String> safeCommand = sanitizedCommand(command);
        try {
            process = startPythonProcess(command, safeCommand);
        } catch (IOException ex) {
            LOGGER.warn(
                    "ChatBot Python process failed to start. command={}, workingDir={}, scriptPath={}, reason={}",
                    safeCommand,
                    chatbot.appDirectory(),
                    chatbot.appPath(),
                    ex.getMessage()
            );
            return RunStatus.START_FAILED;
        }

        long startNanos = System.nanoTime();
        try {
            if (!process.waitFor(processTimeout.toSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                LOGGER.warn(
                        "ChatBot Python process timed out. timeoutSeconds={}, elapsedMs={}, command={}, workingDir={}, scriptPath={}",
                        processTimeout.toSeconds(),
                        elapsedMillis,
                        safeCommand,
                        chatbot.appDirectory(),
                        chatbot.appPath()
                );
                return RunStatus.FAILED;
            }
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            throw ex;
        }

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        int exitCode = process.exitValue();
        boolean outputFileExists = Files.exists(chatbot.outputPath());
        LOGGER.info(
                "ChatBot Python process finished. exitCode={}, elapsedMs={}, outputFileExists={}",
                exitCode,
                elapsedMillis,
                outputFileExists
        );
        if (exitCode != 0) {
            LOGGER.warn(
                    "ChatBot Python process failed. exitCode={}, stdoutOrStderr={}, workingDir={}, command={}, scriptPath={}",
                    exitCode,
                    readProcessLogForDiagnostics(),
                    chatbot.appDirectory(),
                    safeCommand,
                    chatbot.appPath()
            );
        }

        return exitCode == 0 ? RunStatus.SUCCESS : RunStatus.FAILED;
    }

    private String[] buildCommand(String question, String... commandPrefix) {
        String[] command = new String[commandPrefix.length + 3];
        System.arraycopy(commandPrefix, 0, command, 0, commandPrefix.length);
        command[commandPrefix.length] = chatbot.appPath().getFileName().toString();
        command[commandPrefix.length + 1] = "--question";
        command[commandPrefix.length + 2] = question;
        return command;
    }

    private Process startPythonProcess(String[] command, List<String> safeCommand) throws IOException {
        LOGGER.info(
                "Starting ChatBot Python process. command={}, workingDir={}",
                safeCommand,
                chatbot.appDirectory()
        );
        LOGGER.info("ChatBot process script path: {}", chatbot.appPath());
        LOGGER.info("ChatBot process input path: {}", chatbot.inputPath());
        LOGGER.info("ChatBot process output path: {}", chatbot.outputPath());

        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(chatbot.appDirectory().toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.to(chatbot.processLogPath().toFile()));
        processBuilder.environment().put("PYTHONUTF8", "1");
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        return processBuilder.start();
    }

    private List<String> sanitizedCommand(String[] command) {
        List<String> safe = new ArrayList<>();
        for (int i = 0; i < command.length; i++) {
            if (i > 0 && "--question".equals(command[i - 1])) {
                safe.add("<redacted-question>");
            } else {
                safe.add(command[i]);
            }
        }
        return safe;
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

    private static Path resolveChatbotDirectory() {
        String configured = System.getProperty(CHATBOT_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            Path resolved = Path.of(configured).toAbsolutePath().normalize();
            LOGGER.info("ChatBot AI directory resolved from system property {}: {}", CHATBOT_DIR_PROPERTY, resolved);
            return resolved;
        }

        configured = System.getenv(CHATBOT_DIR_ENV);
        if (configured != null && !configured.isBlank()) {
            Path resolved = Path.of(configured).toAbsolutePath().normalize();
            LOGGER.info("ChatBot AI directory resolved from environment variable {}: {}", CHATBOT_DIR_ENV, resolved);
            return resolved;
        }

        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        LOGGER.info("ChatBot AI directory not configured. Searching for {} from working directory {}", DEFAULT_CHATBOT_DIR, current);
        while (current != null) {
            Path candidate = current.resolve(DEFAULT_CHATBOT_DIR).normalize();
            LOGGER.debug("ChatBot AI directory candidate: {}", candidate);
            if (Files.isRegularFile(candidate.resolve("Main").resolve("app.py"))) {
                LOGGER.info("ChatBot AI directory found by parent search: {}", candidate);
                return candidate;
            }
            current = current.getParent();
        }

        Path fallback = Path.of(DEFAULT_CHATBOT_DIR).toAbsolutePath().normalize();
        LOGGER.warn("ChatBot AI directory was not found by parent search. Falling back to relative default: {}", fallback);
        return fallback;
    }

    private static Duration resolveTimeout() {
        String configured = System.getProperty(TIMEOUT_SECONDS_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return parseTimeout(configured, "system property " + TIMEOUT_SECONDS_PROPERTY);
        }

        configured = System.getenv(TIMEOUT_SECONDS_ENV);
        if (configured != null && !configured.isBlank()) {
            return parseTimeout(configured, "environment variable " + TIMEOUT_SECONDS_ENV);
        }

        LOGGER.info("ChatBot timeout using default: {} seconds", DEFAULT_TIMEOUT_SECONDS);
        return Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
    }

    private static Duration parseTimeout(String value, String source) {
        try {
            long seconds = Long.parseLong(value);
            LOGGER.info("ChatBot timeout resolved from {}: {} seconds", source, seconds);
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            LOGGER.warn(
                    "Invalid ChatBot timeout '{}' from {}. Falling back to {} seconds.",
                    value,
                    source,
                    DEFAULT_TIMEOUT_SECONDS
            );
            return Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
        }
    }

    private void logConfiguration() {
        LOGGER.info("ChatBot AI directory resolved: {}", chatbot.directory());
        LOGGER.info("ChatBot Python command resolved: {}", pythonCommand);
        LOGGER.info("ChatBot timeout: {} seconds", processTimeout.toSeconds());
        LOGGER.info("ChatBot input file: {}", chatbot.inputPath());
        LOGGER.info("ChatBot output file: {}", chatbot.outputPath());
        LOGGER.info("ChatBot script file: {}", chatbot.appPath());
        LOGGER.info("ChatBot working directory: {}", chatbot.appDirectory());
        LOGGER.info("ChatBot AI directory exists: {}", Files.isDirectory(chatbot.directory()));
        LOGGER.info("ChatBot input parent directory writable: {}", isWritableDirectory(chatbot.inputPath().getParent()));
        LOGGER.info("ChatBot output parent directory writable: {}", isWritableDirectory(chatbot.outputPath().getParent()));
    }

    private boolean validatePaths() {
        boolean valid = true;
        valid &= validateDirectory("ChatBot AI directory", chatbot.directory());
        valid &= validateDirectory("ChatBot working directory", chatbot.appDirectory());
        valid &= validateParentWritable("ChatBot input parent directory", chatbot.inputPath());
        valid &= validateParentWritable("ChatBot output parent directory", chatbot.outputPath());

        if (!Files.isRegularFile(chatbot.appPath())) {
            LOGGER.warn("ChatBot script path is missing or is not a file: {}", chatbot.appPath());
            valid = false;
        }

        LOGGER.info("ChatBot input file exists: {}", Files.exists(chatbot.inputPath()));
        LOGGER.info("ChatBot output file exists: {}", Files.exists(chatbot.outputPath()));
        return valid;
    }

    private boolean validateDirectory(String label, Path path) {
        if (!Files.exists(path)) {
            LOGGER.warn("{} does not exist: {}", label, path);
            return false;
        }
        if (!Files.isDirectory(path)) {
            LOGGER.warn("{} is not a directory: {}", label, path);
            return false;
        }
        return true;
    }

    private boolean validateParentWritable(String label, Path filePath) {
        Path parent = filePath.getParent();
        if (parent == null) {
            LOGGER.warn("{} cannot be checked because file path has no parent: {}", label, filePath);
            return false;
        }
        if (!Files.exists(parent)) {
            LOGGER.warn("{} does not exist: {}", label, parent);
            return false;
        }
        if (!Files.isDirectory(parent)) {
            LOGGER.warn("{} is not a directory: {}", label, parent);
            return false;
        }
        if (!Files.isWritable(parent)) {
            LOGGER.warn("{} is not writable: {}", label, parent);
            return false;
        }
        return true;
    }

    private static boolean isWritableDirectory(Path path) {
        return path != null && Files.isDirectory(path) && Files.isWritable(path);
    }

    private String readProcessLogForDiagnostics() {
        try {
            if (!Files.isRegularFile(chatbot.processLogPath())) {
                return "";
            }
            return abbreviate(Files.readString(chatbot.processLogPath(), StandardCharsets.UTF_8).trim());
        } catch (IOException ex) {
            return "Unable to read process log: " + ex.getMessage();
        }
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
        } catch (IOException ex) {
            LOGGER.warn("Unable to write ChatBot client error info: {}", chatbot.errorInfoPath(), ex);
        }
    }

    private String abbreviate(String value) {
        if (value == null || value.length() <= MAX_LOG_CHARS) {
            return value;
        }
        return value.substring(0, MAX_LOG_CHARS) + "... [truncated]";
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
            Path relativeInputPath,
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

        private Path inputPath() {
            return directory.resolve(relativeInputPath);
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
