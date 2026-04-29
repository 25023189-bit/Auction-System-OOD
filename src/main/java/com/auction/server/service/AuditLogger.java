package com.auction.server.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs security-related events for audit purposes.
 */
public class AuditLogger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_PREFIX = "[AUDIT]";

    public static void logPasswordResetRequested(String username, String email) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Password reset requested for user: " + username + " (email: " + maskEmail(email) + ")");
    }

    public static void logPasswordResetTokenGenerated(String username, String tokenId) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Reset token generated for user: " + username + " (token: " + tokenId.substring(0, 8) + "...)");
    }

    public static void logPasswordResetSuccess(String username) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Password reset SUCCESS for user: " + username);
    }

    public static void logPasswordResetFailure(String username, String reason) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Password reset FAILED for user: " + username + " - Reason: " + reason);
    }

    public static void logPasswordChanged(String username, String changeType) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Password changed for user: " + username + " (" + changeType + ")");
    }

    public static void logInvalidTokenAttempt(String username, String tokenId) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(LOG_PREFIX + " " + timestamp + " - Invalid/expired token used by user: " + username + " (token: " + tokenId.substring(0, 8) + "...)");
    }

    private static String maskEmail(String email) {
        if (email == null || email.length() < 5) return "***@***";
        String[] parts = email.split("@");
        if (parts.length != 2) return "***@***";

        String name = parts[0];
        String domain = parts[1];
        String maskedName = name.charAt(0) + "*".repeat(Math.max(0, name.length() - 2)) + name.charAt(name.length() - 1);
        String maskedDomain = domain.substring(0, Math.min(2, domain.length())) + "***";

        return maskedName + "@" + maskedDomain;
    }
}
