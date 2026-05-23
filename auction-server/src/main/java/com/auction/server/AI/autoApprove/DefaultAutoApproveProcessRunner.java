package com.auction.server.AI.autoApprove;

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

public class DefaultAutoApproveProcessRunner implements AutoApproveProcessRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAutoApproveProcessRunner.class);
    private static final int MAX_LOG_CHARS = 1_000;

    @Override
    public AutoApproveProcessResult run(
            String pythonCommand,
            Path scriptPath,
            Path inputPath,
            Path outputPath,
            Path workingDirectory,
            Duration timeout
    ) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(pythonCommand);
        command.add(scriptPath.toString());
        command.add("--input");
        command.add(inputPath.toString());
        command.add("--output");
        command.add(outputPath.toString());

        LOGGER.info(
                "Starting AutoApprove Python process. command={}, workingDir={}",
                command,
                workingDirectory
        );
        LOGGER.info("AutoApprove process script path: {}", scriptPath);
        LOGGER.info("AutoApprove process input path: {}", inputPath);
        LOGGER.info("AutoApprove process output path: {}", outputPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        long startNanos = System.nanoTime();
        Process process = processBuilder.start();
        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        if (!finished) {
            process.destroyForcibly();
            LOGGER.warn(
                    "AutoApprove Python process timed out after {} ms. command={}, workingDir={}, scriptPath={}",
                    elapsedMillis,
                    command,
                    workingDirectory,
                    scriptPath
            );
            return AutoApproveProcessResult.timedOut();
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        boolean outputFileExists = Files.exists(outputPath);
        LOGGER.info(
                "AutoApprove Python process finished. exitCode={}, elapsedMs={}, outputFileExists={}",
                process.exitValue(),
                elapsedMillis,
                outputFileExists
        );
        if (process.exitValue() != 0) {
            LOGGER.warn(
                    "AutoApprove Python process failed. exitCode={}, elapsedMs={}, stdoutOrStderr={}, workingDir={}, command={}, scriptPath={}",
                    process.exitValue(),
                    elapsedMillis,
                    abbreviate(output),
                    workingDirectory,
                    command,
                    scriptPath
            );
        }
        return AutoApproveProcessResult.completed(process.exitValue(), output);
    }

    private static String abbreviate(String value) {
        if (value == null || value.length() <= MAX_LOG_CHARS) {
            return value;
        }
        return value.substring(0, MAX_LOG_CHARS) + "... [truncated]";
    }
}
