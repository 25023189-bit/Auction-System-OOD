package com.auction.server.AI.autoApprove;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AutoApproveFileGatewayTest {
    @TempDir
    Path tempDir;

    @Test
    void writeInputWithValidListingCreatesStableSchema() throws Exception {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);

        gateway.writeInput(validInput());

        String json = Files.readString(tempDir.resolve("input_ap.json"), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"schema_version\": 1"));
        assertTrue(json.contains("\"samples\""));
        assertTrue(json.contains("\"title\": \"Vintage Camera\""));
        assertTrue(json.contains("\"start_hour\": 9"));
        assertTrue(json.contains("\"day_of_week\": 0"));
    }

    @Test
    void writeInputRejectsMissingRequiredField() {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);
        AutoApproveListingInput input = new AutoApproveListingInput(
                "",
                "",
                "ORG",
                "Valid description",
                5.0,
                1.0,
                0.0,
                1000.0,
                100.0,
                50.0,
                LocalDateTime.now().plusDays(1),
                60,
                30
        );

        AutoApproveValidationException error = assertThrows(
                AutoApproveValidationException.class,
                () -> gateway.writeInput(input)
        );
        assertEquals("title is required.", error.getMessage());
    }

    @Test
    void readDecisionReturnsTrueForBooleanTrueFile() throws Exception {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);
        Files.writeString(tempDir.resolve("output_ap.json"), "true", StandardCharsets.UTF_8);

        assertTrue(gateway.readDecisionOrFalse());
    }

    @Test
    void readDecisionReturnsFalseForBooleanFalseFile() throws Exception {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);
        Files.writeString(tempDir.resolve("output_ap.json"), "false", StandardCharsets.UTF_8);

        assertFalse(gateway.readDecisionOrFalse());
    }

    @Test
    void readDecisionReturnsFalseWhenOutputDoesNotExist() {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);

        assertFalse(gateway.readDecisionOrFalse());
    }

    @Test
    void readDecisionReturnsFalseWhenOutputIsEmpty() throws Exception {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);
        Files.writeString(tempDir.resolve("output_ap.json"), "", StandardCharsets.UTF_8);

        assertFalse(gateway.readDecisionOrFalse());
    }

    @Test
    void readDecisionReturnsFalseWhenOutputIsInvalid() throws Exception {
        AutoApproveFileGateway gateway = new AutoApproveFileGateway(tempDir);
        Files.writeString(tempDir.resolve("output_ap.json"), "{\"auto_approve\":true}", StandardCharsets.UTF_8);

        assertFalse(gateway.readDecisionOrFalse());
    }

    static AutoApproveListingInput validInput() {
        return new AutoApproveListingInput(
                "Vintage Camera",
                "Collectibles",
                "ORG",
                "Clean camera with full accessories",
                5.0,
                1.0,
                0.0,
                1000.0,
                100.0,
                50.0,
                LocalDateTime.of(2026, 6, 1, 9, 30),
                60,
                30
        );
    }
}
