package com.auction.client.AI.chatbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PythonChatbotConnection {
    private static final PythonChatbotConnection INSTANCE = new PythonChatbotConnection();
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(75);

    private final BotSpec aurora;
    private final BotSpec atlas;
    private final Path finalOutputPath;

    private PythonChatbotConnection() {
        Path projectRoot = locateProjectRoot();
        this.aurora = new BotSpec(
                projectRoot.resolve("Auction_AI").resolve("LLM_Chatbot"),
                Path.of("input.json"),
                Path.of("output.json"),
                "question",
                "answer"
        );
        this.atlas = new BotSpec(
                projectRoot.resolve("Auction_AI").resolve("ChatBot"),
                Path.of("data").resolve("input.json"),
                Path.of("data").resolve("output.json"),
                "message",
                "response"
        );
        this.finalOutputPath = aurora.outputPath();
    }

    public static PythonChatbotConnection getInstance() {
        return INSTANCE;
    }

    public synchronized String ask(String question) {
        boolean interrupted = false;
        String normalizedQuestion = question == null ? "" : question.trim();
        Optional<String> answer = Optional.empty();

        try {
            answer = askBot(aurora, normalizedQuestion);
        } catch (InterruptedException ex) {
            interrupted = true;
        }

        if (answer.isEmpty()) {
            try {
                answer = askBot(atlas, normalizedQuestion);
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }

        String finalAnswer = answer.orElse(ChatbotFallback.MESSAGE);
        writeFinalAnswer(finalAnswer);

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return finalAnswer;
    }

    private Optional<String> askBot(BotSpec bot, String question) throws InterruptedException {
        try {
            validateBotFiles(bot);
            writeJsonStringField(bot.inputPath(), bot.inputField(), question);
            Files.deleteIfExists(bot.outputPath());

            if (!runPythonBot(bot)) {
                return Optional.empty();
            }

            Optional<String> answer = readJsonStringField(bot.outputPath(), bot.outputField())
                    .map(String::trim)
                    .filter(value -> !value.isEmpty());

            if (answer.isEmpty() || looksLikeErrorAnswer(answer.get())) {
                return Optional.empty();
            }

            return answer;
        } catch (IOException | RuntimeException ex) {
            return Optional.empty();
        }
    }

    private void validateBotFiles(BotSpec bot) throws IOException {
        if (!Files.isRegularFile(bot.appPath())) {
            throw new IOException("Missing Python chatbot entrypoint: " + bot.appPath());
        }
        Files.createDirectories(bot.inputPath().getParent());
        Files.createDirectories(bot.outputPath().getParent());
    }

    private boolean runPythonBot(BotSpec bot) throws InterruptedException {
        RunStatus status = runPythonCommand(bot, "python");
        if (status == RunStatus.SUCCESS) {
            return true;
        }

        if (status == RunStatus.START_FAILED) {
            return runPythonCommand(bot, "py", "-3") == RunStatus.SUCCESS;
        }

        return false;
    }

    private RunStatus runPythonCommand(BotSpec bot, String... commandPrefix) throws InterruptedException {
        Process process;
        try {
            process = startPythonProcess(bot, commandPrefix);
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

    private Process startPythonProcess(BotSpec bot, String... commandPrefix) throws IOException {
        String[] command = new String[commandPrefix.length + 1];
        System.arraycopy(commandPrefix, 0, command, 0, commandPrefix.length);
        command[command.length - 1] = bot.appPath().getFileName().toString();

        return new ProcessBuilder(command)
                .directory(bot.directory().toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    private void writeFinalAnswer(String answer) {
        try {
            writeJsonStringField(finalOutputPath, "answer", answer);
        } catch (IOException ignored) {
            // The UI already has the answer. Atlas fallback should not be blocked by a final file write issue.
        }
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

    private void writeJsonStringField(Path path, String field, String value) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String payload = "{\n  \"" + field + "\": \"" + escapeJson(value) + "\"\n}\n";
        Files.writeString(path, payload, StandardCharsets.UTF_8);
    }

    private boolean looksLikeErrorAnswer(String answer) {
        String normalized = Normalizer.normalize(answer, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.startsWith("loi:");
    }

    private Path locateProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            Path auroraDirectory = current.resolve("Auction_AI").resolve("LLM_Chatbot");
            Path atlasDirectory = current.resolve("Auction_AI").resolve("ChatBot");
            if (Files.isDirectory(auroraDirectory) && Files.isDirectory(atlasDirectory)) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

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
            String inputField,
            String outputField
    ) {
        private Path appPath() {
            return directory.resolve("app.py");
        }

        private Path inputPath() {
            return directory.resolve(relativeInputPath);
        }

        private Path outputPath() {
            return directory.resolve(relativeOutputPath);
        }
    }

    private enum RunStatus {
        SUCCESS,
        START_FAILED,
        FAILED
    }
}
