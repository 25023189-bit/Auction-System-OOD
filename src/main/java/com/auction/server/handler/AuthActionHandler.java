package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.ForgotPasswordService;

public class AuthActionHandler extends AbstractClientActionHandler {
    public AuthActionHandler() {
        super("LOGIN", "REGISTER", "RESET_PASSWORD", "FORGOT_PASSWORD");
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        switch (message.getAction()) {
            case "LOGIN" -> handleLogin(message, context);
            case "REGISTER" -> handleRegister(message, context);
            case "RESET_PASSWORD" -> handleResetPassword(message, context);
            case "FORGOT_PASSWORD" -> handleForgotPassword(message, context);
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
            e.printStackTrace();
            context.send(new Message("LOGIN_FAIL", "SERVER", "Login processing error!"));
        }
    }

    private void handleRegister(Message message, ClientActionContext context) {
        try {
            String[] regData = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (regData.length < 7) {
                context.send(new Message("REGISTER_FAIL", "SERVER", "Invalid registration data!"));
                return;
            }

            String customerId = regData[0].trim();
            String username = regData[1].trim();
            String email = regData[2].trim();
            String fullName = regData[3].trim();
            String rawPassword = regData[4].trim();
            String role = regData[5].trim().toUpperCase();
            String organization = regData[6].trim();

            UserDAO userDAO = new UserDAO();
            String generatedCustomerId = userDAO.generateNextCustomerId();

            User user = new User(
                    generatedCustomerId,
                    username,
                    role,
                    "",
                    "SELLER".equals(role) ? organization : null,
                    "BIDDER".equals(role) ? 1_000_000.0 : 0.0
            );
            user.setEmail(email);
            user.setFullName(fullName);

            context.send(context.getAuthService().registerUser(user, rawPassword));
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("REGISTER_FAIL", "SERVER", "Registration processing error!"));
        }
    }

    private void handleResetPassword(Message message, ClientActionContext context) {
        try {
            context.send(context.getAuthService().resetPassword(message.getId(), (String) message.getData()));
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("RESET_FAIL", "SERVER", "Password reset processing error!"));
        }
    }

    private void handleForgotPassword(Message message, ClientActionContext context) {
        try {
            String username = message.getId();
            System.out.println("[AuthActionHandler] Processing FORGOT_PASSWORD for: " + username);
            
            ForgotPasswordService forgotService = new ForgotPasswordService();
            String result = forgotService.processForgotPassword(username);

            switch (result) {
                case "SUCCESS" -> {
                    System.out.println("✅ Forgot password SUCCESS");
                    context.send(new Message("FORGOT_PASSWORD_SUCCESS", "SERVER", 
                        "New password sent to your email!"));
                }
                case "USER_NOT_FOUND" -> context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "Username not found!"));
                case "NO_EMAIL" -> context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "No email address on file!"));
                case "DB_ERROR" -> context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "Database error!"));
                case "EMAIL_FAILED" -> context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "Failed to send email! Check server configuration."));
                default -> context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "Unknown error!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "Error processing request!"));
        }
    }
}
