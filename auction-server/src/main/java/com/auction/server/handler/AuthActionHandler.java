package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.PasswordStrengthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles account authentication actions.
 *
 * Supported protocol:
 * - LOGIN: id=username/customerId, data=password.
 * - REGISTER: data=customerId|username|fullName|password|role|organization.
 * - RESET_PASSWORD: id=username/customerId, data=newPassword:confirmPassword.
 */
public class AuthActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthActionHandler.class);

    private final PasswordStrengthValidator passwordValidator;

    public AuthActionHandler() {
        super("LOGIN", "REGISTER", "RESET_PASSWORD");
        this.passwordValidator = new PasswordStrengthValidator();
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        switch (message.getAction()) {
            case "LOGIN" -> handleLogin(message, context);
            case "REGISTER" -> handleRegister(message, context);
            case "RESET_PASSWORD" -> handleResetPassword(message, context);
            default -> context.send(new Message("UNKNOWN_ACTION", "SERVER", "Unsupported action: " + message.getAction()));
        }
    }

    private void handleLogin(Message message, ClientActionContext context) {
        try {
            String username = message.getId();
            String password = message.getData() != null ? message.getData().toString() : "";

            Message loginResponse = context.getAuthService().login(username, password);
            if ("LOGIN_SUCCESS".equals(loginResponse.getAction()) && loginResponse.getData() instanceof User loggedInUser) {
                context.setUserId(loggedInUser.getId());
            }

            context.send(loginResponse);
        } catch (Exception e) {
            LOGGER.error("Login processing error.", e);
            context.send(new Message("LOGIN_FAIL", "SERVER", "Login processing error!"));
        }
    }

    private void handleRegister(Message message, ClientActionContext context) {
        try {
            String[] regData = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (regData.length < 6) {
                context.send(new Message("REGISTER_FAIL", "SERVER", "Invalid registration data!"));
                return;
            }

            String customerId = regData[0].trim();
            String username = regData[1].trim();
            String fullName = regData[2].trim();
            String rawPassword = regData[3].trim();
            String role = normalizeRole(regData[4]);
            String organization = regData[5].trim();

            if (role == null) {
                context.send(new Message("REGISTER_FAIL", "SERVER", "Invalid role!"));
                return;
            }

            if (!passwordValidator.isStrong(rawPassword)) {
                context.send(new Message("REGISTER_FAIL", "SERVER",
                        "Password is too weak: " + passwordValidator.getLastError()));
                return;
            }

            UserDAO userDAO = new UserDAO();
            String resolvedCustomerId = customerId.isBlank()
                    ? userDAO.generateNextCustomerId()
                    : customerId;

            User user = new User(
                    resolvedCustomerId,
                    username,
                    role,
                    "",
                    "SELLER".equals(role) ? organization : null,
                    "BIDDER".equals(role) ? 1_000_000.0 : 0.0
            );
            user.setFullName(fullName);

            context.send(context.getAuthService().registerUser(user, rawPassword));
        } catch (Exception e) {
            LOGGER.error("Registration processing error.", e);
            context.send(new Message("REGISTER_FAIL", "SERVER", "Registration processing error!"));
        }
    }

    private String normalizeRole(String rawRole) {
        if (rawRole == null) {
            return null;
        }

        String role = rawRole.trim().toUpperCase();
        return switch (role) {
            case "BIDDER", "SELLER", "ADMIN" -> role;
            default -> null;
        };
    }

    private void handleResetPassword(Message message, ClientActionContext context) {
        try {
            context.send(context.getAuthService().resetPassword(message.getId(), (String) message.getData()));
        } catch (Exception e) {
            LOGGER.error("Password reset processing error.", e);
            context.send(new Message("RESET_FAIL", "SERVER", "Password reset processing error!"));
        }
    }
}
