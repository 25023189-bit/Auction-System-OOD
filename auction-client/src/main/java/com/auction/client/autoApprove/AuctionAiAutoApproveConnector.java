package com.auction.client.autoApprove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

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
    private final AutoApproveProcessRunner processRunner;

    public AuctionAiAutoApproveConnector() {
        this(resolveDefaultAiDirectory(), resolvePythonCommand(), resolveTimeout());
    }

    public AuctionAiAutoApproveConnector(Path aiDirectory, String pythonCommand, Duration timeout) {
        this(aiDirectory, pythonCommand, timeout, new DefaultAutoApproveProcessRunner());
    }

    public AuctionAiAutoApproveConnector(
            Path aiDirectory,
            String pythonCommand,
            Duration timeout,
            AutoApproveProcessRunner processRunner
    ) {
        this.aiDirectory = aiDirectory.toAbsolutePath().normalize();
        this.scriptPath = this.aiDirectory.resolve("predict_from_input.py");
        this.pythonCommand = pythonCommand;
        this.timeout = timeout;
        this.fileGateway = new AutoApproveFileGateway(this.aiDirectory);
        this.processRunner = processRunner;
    }

    public boolean requestDecision(AutoApproveListingInput input) {
        try {
            fileGateway.writeInput(input);
            clearPreviousOutput();

            if (!Files.exists(scriptPath)) {
                LOGGER.warn("Auto approve script not found: {}", scriptPath);
                return false;
            }

            AutoApproveProcessResult processResult = processRunner.run(
                    pythonCommand,
                    scriptPath,
                    fileGateway.getInputPath(),
                    fileGateway.getOutputPath(),
                    aiDirectory,
                    timeout
            );
            if (processResult.isTimedOut()) {
                LOGGER.warn("Auto approve AI timed out after {} ms.", timeout.toMillis());
                return false;
            }

            if (processResult.getExitCode() != 0) {
                LOGGER.warn(
                        "Auto approve AI exited with code {}. Output: {}",
                        processResult.getExitCode(),
                        processResult.getOutput()
                );
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

    private void clearPreviousOutput() {
        try {
            Files.deleteIfExists(fileGateway.getOutputPath());
        } catch (IOException e) {
            LOGGER.warn("Unable to clear previous auto approve output: {}", fileGateway.getOutputPath(), e);
        }
    }

    private static Path resolveDefaultAiDirectory() {
        String configuredPath = System.getProperty(AI_DIR_PROPERTY);
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath);
        }

        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve(DEFAULT_AI_DIR);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }

        return Path.of(DEFAULT_AI_DIR);
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
