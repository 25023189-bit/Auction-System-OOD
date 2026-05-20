package com.auction.client.AI.auto_approve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuctionAiAutoApproveConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionAiAutoApproveConnector.class);

    private static final String DEFAULT_AI_DIR = "Auction_AI/AutoApprove";
    private static final String PYTHON_PROPERTY = "auction.autoApprove.python";
    private static final String AI_DIR_PROPERTY = "auction.autoApprove.dir";
    private static final String TIMEOUT_SECONDS_PROPERTY = "auction.autoApprove.timeoutSeconds";

    private final Path aiDirectory;
    private final Path scriptPath;
    private final String pythonCommand;
    private final Duration timeout;
    private final AutoApproveFileGateway fileGateway;

    public AuctionAiAutoApproveConnector() {
        this(resolveDefaultAiDirectory(), resolvePythonCommand(), resolveTimeout());
    }

    public AuctionAiAutoApproveConnector(Path aiDirectory, String pythonCommand, Duration timeout) {
        this.aiDirectory = aiDirectory.toAbsolutePath().normalize();
        this.scriptPath = this.aiDirectory.resolve("predict_from_input.py");
        this.pythonCommand = pythonCommand;
        this.timeout = timeout;
        this.fileGateway = new AutoApproveFileGateway(this.aiDirectory);
    }

    public boolean requestDecision(AutoApproveListingInput input) {
        try {
            fileGateway.writeInput(input);
            clearPreviousOutput();

            if (!Files.exists(scriptPath)) {
                LOGGER.warn("Auto approve script not found: {}", scriptPath);
                return false;
            }

            Process process = startProcess();
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                LOGGER.warn("Auto approve AI timed out after {} ms.", timeout.toMillis());
                return false;
            }

            String processOutput = readProcessOutput(process);
            if (process.exitValue() != 0) {
                LOGGER.warn("Auto approve AI exited with code {}. Output: {}", process.exitValue(), processOutput);
                return false;
            }

            return fileGateway.readDecisionOrFalse();
        } catch (AutoApproveValidationException e) {
            LOGGER.warn("Auto approve input validation failed: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Auto approve AI process was interrupted.", e);
            return false;
        } catch (Exception e) {
            LOGGER.warn("Auto approve AI connector failed. Falling back to manual approval.", e);
            return false;
        }
    }

    private Process startProcess() throws IOException {
        List<String> command = new ArrayList<>();
        command.add(pythonCommand);
        command.add(scriptPath.toString());
        command.add("--input");
        command.add(fileGateway.getInputPath().toString());
        command.add("--output");
        command.add(fileGateway.getOutputPath().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(aiDirectory.toFile());
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private String readProcessOutput(Process process) {
        try {
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            LOGGER.warn("Unable to read auto approve AI process output.", e);
            return "";
        }
    }

    private void clearPreviousOutput() {
        try {
            Files.deleteIfExists(fileGateway.getOutputPath());
        } catch (IOException e) {
            LOGGER.warn("Unable to clear previous auto approve output: {}", fileGateway.getOutputPath(), e);
        }
    }

    private static Path resolveDefaultAiDirectory() {
        return Path.of(System.getProperty(AI_DIR_PROPERTY, DEFAULT_AI_DIR));
    }

    private static String resolvePythonCommand() {
        return System.getProperty(PYTHON_PROPERTY, "python");
    }

    private static Duration resolveTimeout() {
        String configuredSeconds = System.getProperty(TIMEOUT_SECONDS_PROPERTY, "10");
        try {
            long seconds = Long.parseLong(configuredSeconds);
            return Duration.ofSeconds(Math.max(1, seconds));
        } catch (NumberFormatException e) {
            return Duration.ofSeconds(10);
        }
    }
}
