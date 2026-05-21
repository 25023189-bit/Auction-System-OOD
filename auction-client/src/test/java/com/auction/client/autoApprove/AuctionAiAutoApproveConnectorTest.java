package com.auction.client.autoApprove;

import com.auction.client.AI.autoApprove.AuctionAiAutoApproveConnector;
import com.auction.client.AI.autoApprove.AutoApproveListingInput;
import com.auction.client.AI.autoApprove.AutoApproveProcessResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AuctionAiAutoApproveConnectorTest {
    @TempDir
    Path tempDir;

    @Test
    void requestDecisionReturnsTrueWhenAiWritesTrue() throws Exception {
        createScriptMarker();
        AuctionAiAutoApproveConnector connector = connectorWithRunner((outputPath) -> {
            Files.writeString(outputPath, "true", StandardCharsets.UTF_8);
            return AutoApproveProcessResult.completed(0, "");
        });

        assertTrue(connector.requestDecision(AutoApproveFileGatewayTest.validInput()));
    }

    @Test
    void requestDecisionReturnsFalseWhenAiWritesFalse() throws Exception {
        createScriptMarker();
        AuctionAiAutoApproveConnector connector = connectorWithRunner((outputPath) -> {
            Files.writeString(outputPath, "false", StandardCharsets.UTF_8);
            return AutoApproveProcessResult.completed(0, "");
        });

        assertFalse(connector.requestDecision(AutoApproveFileGatewayTest.validInput()));
    }

    @Test
    void requestDecisionReturnsFalseWhenOutputFileIsMissing() throws Exception {
        createScriptMarker();
        AuctionAiAutoApproveConnector connector = connectorWithRunner((outputPath) ->
                AutoApproveProcessResult.completed(0, "")
        );

        assertFalse(connector.requestDecision(AutoApproveFileGatewayTest.validInput()));
    }

    @Test
    void requestDecisionReturnsFalseOnAiTimeout() throws Exception {
        createScriptMarker();
        AuctionAiAutoApproveConnector connector = connectorWithRunner((outputPath) ->
                AutoApproveProcessResult.timedOut()
        );

        assertFalse(connector.requestDecision(AutoApproveFileGatewayTest.validInput()));
    }

    @Test
    void requestDecisionReturnsFalseOnAiProcessError() throws Exception {
        createScriptMarker();
        AuctionAiAutoApproveConnector connector = connectorWithRunner((outputPath) ->
                AutoApproveProcessResult.completed(1, "boom")
        );

        assertFalse(connector.requestDecision(AutoApproveFileGatewayTest.validInput()));
    }

    @Test
    void requestDecisionDoesNotRunProcessWhenInputValidationFails() throws Exception {
        createScriptMarker();
        AtomicBoolean processCalled = new AtomicBoolean(false);
        AuctionAiAutoApproveConnector connector = new AuctionAiAutoApproveConnector(
                tempDir,
                "python",
                Duration.ofSeconds(1),
                (pythonCommand, scriptPath, inputPath, outputPath, workingDirectory, timeout) -> {
                    processCalled.set(true);
                    return AutoApproveProcessResult.completed(0, "");
                }
        );

        AutoApproveListingInput invalidInput = new AutoApproveListingInput(
                "",
                "",
                "ORG",
                "Description",
                5.0,
                1.0,
                0.0,
                1000.0,
                100.0,
                50.0,
                AutoApproveFileGatewayTest.validInput().getStartTime(),
                60,
                30
        );

        assertFalse(connector.requestDecision(invalidInput));
        assertFalse(processCalled.get());
    }

    private AuctionAiAutoApproveConnector connectorWithRunner(FakeRunner runner) {
        return new AuctionAiAutoApproveConnector(
                tempDir,
                "python",
                Duration.ofSeconds(1),
                (pythonCommand, scriptPath, inputPath, outputPath, workingDirectory, timeout) ->
                        runner.run(outputPath)
        );
    }

    private void createScriptMarker() throws Exception {
        Files.writeString(tempDir.resolve("predict_from_input.py"), "# fake", StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface FakeRunner {
        AutoApproveProcessResult run(Path outputPath) throws Exception;
    }
}
