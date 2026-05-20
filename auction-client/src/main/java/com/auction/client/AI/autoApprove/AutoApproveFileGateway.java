package com.auction.client.AI.autoApprove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;

public class AutoApproveFileGateway {
    public static final String INPUT_FILE_NAME = "input_ap.json";
    public static final String OUTPUT_FILE_NAME = "output_ap.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoApproveFileGateway.class);

    private final Path inputPath;
    private final Path outputPath;
    private final AutoApproveInputValidator inputValidator;
    private final AutoApproveOutputValidator outputValidator;

    public AutoApproveFileGateway(Path aiDirectory) {
        this(
                aiDirectory.resolve(INPUT_FILE_NAME),
                aiDirectory.resolve(OUTPUT_FILE_NAME),
                new AutoApproveInputValidator(),
                new AutoApproveOutputValidator()
        );
    }

    public AutoApproveFileGateway(
            Path inputPath,
            Path outputPath,
            AutoApproveInputValidator inputValidator,
            AutoApproveOutputValidator outputValidator
    ) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.inputValidator = inputValidator;
        this.outputValidator = outputValidator;
    }

    public void writeInput(AutoApproveListingInput input) throws AutoApproveValidationException, IOException {
        inputValidator.validate(input);
        Files.createDirectories(inputPath.getParent());
        Files.writeString(inputPath, toJson(input), StandardCharsets.UTF_8);
    }

    public boolean readDecisionOrFalse() {
        try {
            if (!Files.exists(outputPath)) {
                LOGGER.warn("Auto approve output file does not exist: {}", outputPath);
                return false;
            }
            String rawValue = Files.readString(outputPath, StandardCharsets.UTF_8);
            return outputValidator.parseDecision(rawValue);
        } catch (Exception e) {
            LOGGER.warn("Invalid auto approve output. Falling back to manual approval.", e);
            return false;
        }
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    private String toJson(AutoApproveListingInput input) {
        int startHour = input.getStartTime().getHour();
        int dayOfWeek = toPythonDayOfWeek(input.getStartTime().getDayOfWeek());
        int isWeekend = dayOfWeek >= 5 ? 1 : 0;
        double startPriceLog = Math.log(input.getStartPrice());
        double minimumJoinRatio = input.getMinimumJoinAmount() / input.getStartPrice();
        double bidStepRatio = input.getBidStep() / input.getStartPrice();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema_version\": ").append(AutoApproveListingInput.SCHEMA_VERSION).append(",\n");
        json.append("  \"source\": \"auction-system\",\n");
        json.append("  \"samples\": [\n");
        json.append("    {\n");
        appendString(json, "title", input.getTitle(), true);
        appendString(json, "category", input.getCategory(), true);
        appendString(json, "organization", input.getOrganization(), true);
        appendString(json, "description", input.getDescription(), true);
        appendNumber(json, "seller_rating", input.getSellerRating(), true);
        appendNumber(json, "seller_completed_rating", input.getSellerCompletedRating(), true);
        appendNumber(json, "seller_cancel_rate", input.getSellerCancelRate(), true);
        appendNumber(json, "minimum_join_amount", input.getMinimumJoinAmount(), true);
        appendNumber(json, "minimum_join_ratio", minimumJoinRatio, true);
        appendNumber(json, "bid_step", input.getBidStep(), true);
        appendNumber(json, "bid_step_ratio", bidStepRatio, true);
        appendNumber(json, "start_price", input.getStartPrice(), true);
        appendNumber(json, "start_price_log", startPriceLog, true);
        appendNumber(json, "duration_minutes", input.getDurationMinutes(), true);
        appendNumber(json, "extension_seconds", input.getExtensionSeconds(), true);
        appendNumber(json, "start_hour", startHour, true);
        appendNumber(json, "day_of_week", dayOfWeek, true);
        appendNumber(json, "is_weekend", isWeekend, true);
        appendNumber(json, "title_length", input.getTitle().length(), true);
        appendNumber(json, "desc_length", input.getDescription().length(), false);
        json.append("    }\n");
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private int toPythonDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() - 1;
    }

    private void appendString(StringBuilder json, String field, String value, boolean comma) {
        json.append("      \"")
                .append(field)
                .append("\": \"")
                .append(escape(value))
                .append("\"");
        appendLineEnd(json, comma);
    }

    private void appendNumber(StringBuilder json, String field, double value, boolean comma) {
        json.append("      \"").append(field).append("\": ").append(value);
        appendLineEnd(json, comma);
    }

    private void appendLineEnd(StringBuilder json, boolean comma) {
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
