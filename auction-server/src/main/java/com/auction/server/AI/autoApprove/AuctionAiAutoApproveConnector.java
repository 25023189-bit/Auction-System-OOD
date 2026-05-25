package com.auction.server.AI.autoApprove;

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
    private static final String AI_DIR_ENV = "AUCTION_AUTO_APPROVE_DIR";
    private static final String PYTHON_ENV = "AUCTION_PYTHON_COMMAND";
    private static final String TIMEOUT_SECONDS_ENV = "AUCTION_AUTO_APPROVE_TIMEOUT_SECONDS";

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
        logConfiguration();
        validatePaths();
    }

    public boolean requestDecision(AutoApproveListingInput input) {
        try {
            fileGateway.writeInput(input);
            clearPreviousOutput();

            if (!validatePaths()) {
                LOGGER.warn("AutoApprove path diagnostics failed. Falling back to manual approval.");
                return false;
            }

            if (!Files.isRegularFile(scriptPath)) {
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
                        "Auto approve AI exited with code {}. stdoutOrStderr={}",
                        processResult.getExitCode(),
                        abbreviate(processResult.getOutput())
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

    private void logConfiguration() {
        LOGGER.info("AutoApprove AI directory resolved: {}", aiDirectory);
        LOGGER.info("AutoApprove Python command resolved: {}", pythonCommand);
        LOGGER.info("AutoApprove timeout: {} seconds", timeout.toSeconds());
        LOGGER.info("AutoApprove input file: {}", fileGateway.getInputPath());
        LOGGER.info("AutoApprove output file: {}", fileGateway.getOutputPath());
        LOGGER.info("AutoApprove script file: {}", scriptPath);
        LOGGER.info("AutoApprove AI directory exists: {}", Files.isDirectory(aiDirectory));
        LOGGER.info("AutoApprove input parent directory writable: {}", isWritableDirectory(fileGateway.getInputPath().getParent()));
    }

    private boolean validatePaths() {
        boolean valid = true;
        valid &= validateDirectory("AutoApprove AI directory", aiDirectory);
        valid &= validateParentWritable("AutoApprove input parent directory", fileGateway.getInputPath());
        valid &= validateParentWritable("AutoApprove output parent directory", fileGateway.getOutputPath());

        if (!Files.isRegularFile(scriptPath)) {
            LOGGER.warn("AutoApprove script path is missing or is not a file: {}", scriptPath);
            valid = false;
        }

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

    private static Path resolveDefaultAiDirectory() {
        String configuredDir = System.getProperty(AI_DIR_PROPERTY);
        if (configuredDir != null && !configuredDir.isBlank()) {
            Path resolved = Path.of(configuredDir).toAbsolutePath().normalize();
            LOGGER.info("AutoApprove AI directory resolved from system property {}: {}", AI_DIR_PROPERTY, resolved);
            return resolved;
        }

        configuredDir = System.getenv(AI_DIR_ENV);
        if (configuredDir != null && !configuredDir.isBlank()) {
            Path resolved = Path.of(configuredDir).toAbsolutePath().normalize();
            LOGGER.info("AutoApprove AI directory resolved from environment variable {}: {}", AI_DIR_ENV, resolved);
            return resolved;
        }

        Path current = Path.of("").toAbsolutePath().normalize();
        LOGGER.info("AutoApprove AI directory not configured. Searching for {} from working directory {}", DEFAULT_AI_DIR, current);

        while (current != null) {
            Path candidate = current.resolve(DEFAULT_AI_DIR).normalize();
            LOGGER.debug("AutoApprove AI directory candidate: {}", candidate);

            if (Files.isDirectory(candidate)) {
                LOGGER.info("AutoApprove AI directory found by parent search: {}", candidate);
                return candidate;
            }

            current = current.getParent();
        }

        Path fallback = Path.of(DEFAULT_AI_DIR).toAbsolutePath().normalize();
        LOGGER.warn("AutoApprove AI directory was not found by parent search. Falling back to relative default: {}", fallback);
        return fallback;
    }

    private static String resolvePythonCommand() {
        String configuredPython = System.getProperty(PYTHON_PROPERTY);
        if (configuredPython != null && !configuredPython.isBlank()) {
            LOGGER.info("AutoApprove Python command resolved from system property {}: {}", PYTHON_PROPERTY, configuredPython);
            return configuredPython;
        }

        configuredPython = System.getenv(PYTHON_ENV);
        if (configuredPython != null && !configuredPython.isBlank()) {
            LOGGER.info("AutoApprove Python command resolved from environment variable {}: {}", PYTHON_ENV, configuredPython);
            return configuredPython;
        }

        LOGGER.info("AutoApprove Python command using default: python");
        return "python";
    }

    private static Duration resolveTimeout() {
        String configuredTimeout = System.getProperty(TIMEOUT_SECONDS_PROPERTY);
        if (configuredTimeout != null && !configuredTimeout.isBlank()) {
            return parseTimeout(configuredTimeout, "system property " + TIMEOUT_SECONDS_PROPERTY);
        }

        configuredTimeout = System.getenv(TIMEOUT_SECONDS_ENV);
        if (configuredTimeout != null && !configuredTimeout.isBlank()) {
            return parseTimeout(configuredTimeout, "environment variable " + TIMEOUT_SECONDS_ENV);
        }

        LOGGER.info("AutoApprove timeout using default: 10 seconds");
        return Duration.ofSeconds(10);
    }

    private static Duration parseTimeout(String value, String source) {
        try {
            long seconds = Long.parseLong(value);
            LOGGER.info("AutoApprove timeout resolved from {}: {} seconds", source, seconds);
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid AutoApprove timeout '{}' from {}. Falling back to 10 seconds.", value, source);
            return Duration.ofSeconds(10);
        }
    }

    private static String abbreviate(String value) {
        if (value == null || value.length() <= 1_000) {
            return value;
        }
        return value.substring(0, 1_000) + "... [truncated]";
    }
}
