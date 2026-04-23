package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;

public class AuthActionHandler extends AbstractClientActionHandler {
    public AuthActionHandler() {
        super("LOGIN", "REGISTER", "RESET_PASSWORD");
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
            e.printStackTrace();
            context.send(new Message("LOGIN_FAIL", "SERVER", "Login processing error!"));
        }
    }

    private void handleRegister(Message message, ClientActionContext context) {
        try {
            String[] regData = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (regData.length < 4) {
                context.send(new Message("REGISTER_FAIL", "SERVER", "Invalid registration data!"));
                return;
            }

            String username = regData[0].trim();
            String rawPassword = regData[1].trim();
            String role = regData[2].trim().toUpperCase();
            String organization = regData[3].trim();

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
}
