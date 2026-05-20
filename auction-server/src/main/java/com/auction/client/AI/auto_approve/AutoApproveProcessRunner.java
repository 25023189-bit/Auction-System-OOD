package com.auction.client.AI.auto_approve;

import java.nio.file.Path;
import java.time.Duration;

public interface AutoApproveProcessRunner {
    AutoApproveProcessResult run(
            String pythonCommand,
            Path scriptPath,
            Path inputPath,
            Path outputPath,
            Path workingDirectory,
            Duration timeout
    ) throws Exception;
}
