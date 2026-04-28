package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.ForgotPasswordService;
import com.auction.server.service.PasswordStrengthValidator;

public class AuthActionHandler extends AbstractClientActionHandler {
    private final ForgotPasswordService forgotPasswordService;
    private final PasswordStrengthValidator passwordValidator;

    public AuthActionHandler() {
        super("LOGIN", "REGISTER", "RESET_PASSWORD", "FORGOT_PASSWORD", "VERIFY_OTP", "UPDATE_NEW_PASSWORD");
        this.forgotPasswordService = new ForgotPasswordService();
        this.passwordValidator = new PasswordStrengthValidator();
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        switch (message.getAction()) {
            case "LOGIN" -> handleLogin(message, context);
            case "REGISTER" -> handleRegister(message, context);
            case "RESET_PASSWORD" -> handleResetPassword(message, context);
            case "FORGOT_PASSWORD" -> handleForgotPassword(message, context);
            case "VERIFY_OTP" -> handleVerifyOTP(message, context);
            case "UPDATE_NEW_PASSWORD" -> handleUpdateNewPassword(message, context);
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

            // Validate password strength
            if (!passwordValidator.isStrong(rawPassword)) {
                context.send(new Message("REGISTER_FAIL", "SERVER", 
                    "Password is too weak: " + passwordValidator.getLastError()));
                return;
            }

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
            
            String result = forgotPasswordService.processForgotPassword(username);

            if (result.startsWith("SUCCESS")) {
                String[] parts = result.split("\\|");
                String tokenId = parts.length > 1 ? parts[1] : "";
                System.out.println("✅ OTP sent successfully");
                context.send(new Message("FORGOT_PASSWORD_OTP_SENT", "SERVER", tokenId));
            } else if ("USER_NOT_FOUND".equals(result)) {
                context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "Username or Customer ID not found!"));
            } else if ("NO_EMAIL".equals(result)) {
                context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "No email address registered for this account!"));
            } else if ("RATE_LIMITED".equals(result)) {
                int remaining = forgotPasswordService.getRemainingRequests(username);
                context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "Too many reset requests. Please try again in 5 minutes. (" + remaining + " requests remaining)"));
            } else if ("EMAIL_FAILED".equals(result)) {
                context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", 
                    "Failed to send email. Please check server configuration or try again later."));
            } else {
                context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "Unknown error occurred!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("FORGOT_PASSWORD_FAIL", "SERVER", "Error processing request!"));
        }
    }

    private void handleVerifyOTP(Message message, ClientActionContext context) {
        try {
            String[] data = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (data.length < 2) {
                context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "Invalid OTP data!"));
                return;
            }

            String tokenId = data[0].trim();
            String otp = data[1].trim();

            String result = forgotPasswordService.verifyOTPAndGetTemporaryPassword(tokenId, otp);

            if (result.startsWith("SUCCESS")) {
                String[] parts = result.split("\\|");
                String tempPassword = parts.length > 1 ? parts[1] : "";
                context.send(new Message("OTP_VERIFY_SUCCESS", "SERVER", tempPassword));
            } else if ("INVALID_OTP".equals(result)) {
                context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "Invalid OTP code!"));
            } else if ("EXPIRED_TOKEN".equals(result)) {
                context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "OTP has expired. Please request a new one."));
            } else if ("INVALID_TOKEN".equals(result)) {
                context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "Invalid token!"));
            } else {
                context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "Error verifying OTP!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("OTP_VERIFY_FAIL", "SERVER", "OTP verification error!"));
        }
    }

    private void handleUpdateNewPassword(Message message, ClientActionContext context) {
        try {
            String[] data = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (data.length < 3) {
                context.send(new Message("PASSWORD_UPDATE_FAIL", "SERVER", "Invalid password data!"));
                return;
            }

            String userId = message.getId();
            String newPassword = data[0].trim();
            String confirmPassword = data[1].trim();

            String result = forgotPasswordService.validateAndUpdatePassword(userId, newPassword, confirmPassword);

            if ("SUCCESS".equals(result)) {
                context.send(new Message("PASSWORD_UPDATE_SUCCESS", "SERVER", "Password changed successfully!"));
            } else if ("PASSWORDS_DONT_MATCH".equals(result)) {
                context.send(new Message("PASSWORD_UPDATE_FAIL", "SERVER", "Passwords do not match!"));
            } else if (result.startsWith("WEAK_PASSWORD")) {
                String[] parts = result.split("\\|");
                String reason = parts.length > 1 ? parts[1] : "Password is too weak";
                context.send(new Message("PASSWORD_UPDATE_FAIL", "SERVER", reason));
            } else {
                context.send(new Message("PASSWORD_UPDATE_FAIL", "SERVER", "Error updating password!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.send(new Message("PASSWORD_UPDATE_FAIL", "SERVER", "Password update error!"));
        }
    }
}
