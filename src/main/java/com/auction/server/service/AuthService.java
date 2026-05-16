package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.IAuctionDAO;
import com.auction.server.dao.IUserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service nghiệp vụ cho xác thực, đăng ký và reset mật khẩu cơ bản.
 *
 * Vai trò:
 * - Gọi UserDAO để login, register và reset password.
 * - Bổ sung thống kê seller vào User trước khi trả LOGIN_SUCCESS.
 *
 * Luồng chính:
 * 1. AuthActionHandler truyền input đã parse vào AuthService.
 * 2. Service gọi DAO, map kết quả DAO thành Message response cho client.
 *
 * Business rules:
 * - Login chấp nhận username hoặc customer_id và chỉ thành công khi BCrypt verify pass.
 * - Seller login thành công phải có tỷ lệ đấu giá thành công/hủy bởi admin để client hiển thị đúng.
 *
 * Ghi chú kỹ thuật:
 * - Sử dụng dependency injection với interfaces (IUserDAO, IAuctionDAO) để dễ test.
 * - Dependency: IUserDAO, IAuctionDAO, Message, User, SLF4J.
 */
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final IUserDAO userDAO;
    private final IAuctionDAO auctionDAO;

    // Constructor cho dependency injection (sử dụng trong tests)
    public AuthService(IUserDAO userDAO, IAuctionDAO auctionDAO) {
        this.userDAO = userDAO;
        this.auctionDAO = auctionDAO;
    }

    // Default constructor cho production (nếu không inject)
    public AuthService() {
        this.userDAO = new com.auction.server.dao.UserDAO();
        this.auctionDAO = new com.auction.server.dao.AuctionDAO();
    }

    public Message login(String loginId, String password) {
        LOGGER.info("Login request. loginId={}", loginId);

        // loginId có thể là username hoặc customer_id; DAO chịu trách nhiệm chuẩn hóa.
        User user = userDAO.login(loginId, password);

        if (user != null) {
            // Seller cần thêm thống kê để client/seller validator hiển thị đúng uy tín.
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
        // Chỉ seller mới cần tỷ lệ thành công/hủy bởi admin.
        if (user == null || !"SELLER".equalsIgnoreCase(user.getRole())) {
            return;
        }

        IAuctionDAO.SellerAuctionStats stats = auctionDAO.getSellerAuctionStats(user.getId());
        user.setSuccessfulAuctionRate(stats.getSuccessfulAuctionRate());
        user.setAdminCancellationRate(stats.getAdminCancellationRate());
    }
}
