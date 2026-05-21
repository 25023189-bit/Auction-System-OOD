package com.auction.server.dao;

import com.auction.common.model.User;
import java.util.List;

/**
 * Interface for UserDAO - defines contract for user data access operations.
 * Enables dependency injection and mocking in tests.
 */
public interface IUserDAO {
    User getUserById(String customerId);
    User getUserByUsername(String username);
    User login(String username, String rawPassword);
    String registerUser(User user, String rawPassword);
    boolean resetPassword(String customerId, String newPassword, String confirmPassword);
    String resetPasswordWithNewPassword(String username, String newPassword);
    List<User> getAllUsers();
    boolean deleteUser(String customerId);
    String generateNextCustomerId();
}
