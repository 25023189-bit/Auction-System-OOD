package com.auction.client.AI.autoApprove;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultAutoApproveProcessRunner implements AutoApproveProcessRunner {
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

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            return AutoApproveProcessResult.timedOut();
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        return AutoApproveProcessResult.completed(process.exitValue(), output);
    }
}
