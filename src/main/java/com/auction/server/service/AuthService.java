package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    public Message login(String loginId, String password) {
        System.out.println("\n[AuthService] Login request:");
        System.out.println("  - Login ID (Username/CustomerID): " + loginId);

        // Truyền thẳng dữ liệu nhập vào (có thể là ID hoặc Username) xuống DAO
        User user = userDAO.login(loginId, password);

        if (user != null) {
            applySellerAuctionStats(user);
            System.out.println("  - Result: SUCCESS (Role: " + user.getRole() + ")");
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        }

        System.out.println("  - Result: FAILED");
        return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản (ID/Username) hoặc mật khẩu!");
    }

    public Message registerUser(User user, String rawPassword) {
        System.out.println("\n[AuthService] Register request:");
        System.out.println("  - New ID: " + user.getId());
        System.out.println("  - New username: " + user.getUsername());

        String resultStatus = userDAO.registerUser(user, rawPassword);

        if ("SUCCESS".equals(resultStatus)) {
            System.out.println(" - Result: REGISTER SUCCESS");
            return new Message("REGISTER_SUCCESS", "SERVER", user.getUsername());
        } else if ("DUPLICATE".equals(resultStatus)) {
            System.out.println(" - Result: DUPLICATE DATA");
            return new Message("REGISTER_FAIL", "SERVER", "Username is already in use!");
        } else {
            System.out.println(" - Result: DATABASE ERROR - " + resultStatus);
            return new Message("REGISTER_FAIL", "SERVER", resultStatus);
        }
    }

    public Message resetPassword(String customerId, String data) {
        System.out.println("\n[AuthService] Password reset request for: " + customerId);

        String[] parts = data.split(":");
        if (parts.length < 2) {
            return new Message("RESET_FAIL", "SERVER", "Invalid request data!");
        }

        String newPassword = parts[0];
        String confirmPassword = parts[1];

        boolean isSuccess = userDAO.resetPassword(customerId, newPassword, confirmPassword);

        if (isSuccess) {
            System.out.println("  - Result: PASSWORD RESET SUCCESS");
            return new Message("RESET_SUCCESS", "SERVER", "Password changed successfully!");
        }

        System.out.println("  - Result: PASSWORD RESET FAILED");
        return new Message("RESET_FAIL", "SERVER", "Unable to change password. Please check your data and try again!");
    }

    private void applySellerAuctionStats(User user) {
        if (user == null || !"SELLER".equalsIgnoreCase(user.getRole())) {
            return;
        }

        AuctionDAO.SellerAuctionStats stats = auctionDAO.getSellerAuctionStats(user.getId());
        user.setSuccessfulAuctionRate(stats.getSuccessfulAuctionRate());
        user.setAdminCancellationRate(stats.getAdminCancellationRate());
    }
}
