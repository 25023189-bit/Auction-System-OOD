package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO = new UserDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    public Message login(String loginId, String password) {
        LOGGER.info("Login request. loginId={}", loginId);

        // Truyền thẳng dữ liệu nhập vào (có thể là ID hoặc Username) xuống DAO
        User user = userDAO.login(loginId, password);

        if (user != null) {
            applySellerAuctionStats(user);
            LOGGER.info("Login result: SUCCESS. role={}", user.getRole());
            return new Message("LOGIN_SUCCESS", "SERVER", user);
        }

        LOGGER.info("Login result: FAILED.");
        return new Message("LOGIN_FAIL", "SERVER", "Sai tài khoản (ID/Username) hoặc mật khẩu!");
    }

    public Message registerUser(User user, String rawPassword) {
        LOGGER.info("Register request. id={}, username={}", user.getId(), user.getUsername());

        String resultStatus = userDAO.registerUser(user, rawPassword);

        if ("SUCCESS".equals(resultStatus)) {
            LOGGER.info("Register result: SUCCESS.");
            return new Message("REGISTER_SUCCESS", "SERVER", user.getUsername());
        } else if ("DUPLICATE".equals(resultStatus)) {
            LOGGER.info("Register result: DUPLICATE DATA.");
            return new Message("REGISTER_FAIL", "SERVER", "Username is already in use!");
        } else {
            LOGGER.warn("Register result: DATABASE ERROR - {}", resultStatus);
            return new Message("REGISTER_FAIL", "SERVER", resultStatus);
        }
    }

    public Message resetPassword(String customerId, String data) {
        LOGGER.info("Password reset request for: {}", customerId);

        String[] parts = data.split(":");
        if (parts.length < 2) {
            return new Message("RESET_FAIL", "SERVER", "Invalid request data!");
        }

        String newPassword = parts[0];
        String confirmPassword = parts[1];

        boolean isSuccess = userDAO.resetPassword(customerId, newPassword, confirmPassword);

        if (isSuccess) {
            LOGGER.info("Password reset result: SUCCESS.");
            return new Message("RESET_SUCCESS", "SERVER", "Password changed successfully!");
        }

        LOGGER.info("Password reset result: FAILED.");
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
